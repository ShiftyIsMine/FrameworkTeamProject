package kr.ac.kopo.service;

import kr.ac.kopo.dto.SpotifyTrackDto;
import kr.ac.kopo.entity.Song;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@ConditionalOnBean(SpotifyApi.class)
public class SpotifyService {

    @Autowired
    private SpotifyApi spotifyApi;

    private String accessToken;
    private long tokenExpirationTime;

    private void refreshAccessToken() {
        try {
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            accessToken = clientCredentials.getAccessToken();
            tokenExpirationTime = System.currentTimeMillis() + (clientCredentials.getExpiresIn() * 1000);

            spotifyApi.setAccessToken(accessToken);


        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            log.error(e.getMessage());
        }
    }


    private void ensureValidToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime - 60000) {
            refreshAccessToken();
        }
    }

    public List<SpotifyTrackDto> searchTracks(String artist, String title) {
        try {
            ensureValidToken();

            String query = artist + " " + title;

            SearchTracksRequest searchTracksRequest = spotifyApi
                    .searchTracks(query)
                    .limit(10)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            List<SpotifyTrackDto> tracks = new ArrayList<>();
            for (Track track : trackPaging.getItems()) {
                tracks.add(convertToDto(track));
            }

            return tracks;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    //아티스트로만 검색
    public List<SpotifyTrackDto> searchByArtist(String artistName) {
        try {
            ensureValidToken();

            SearchTracksRequest searchTracksRequest = spotifyApi
                    .searchTracks("artist:" + artistName)
                    .limit(20)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            List<SpotifyTrackDto> tracks = new ArrayList<>();
            for (Track track : trackPaging.getItems()) {
                tracks.add(convertToDto(track));
            }


            return tracks;

        } catch (Exception e) {
            log.error("아티스트 검색 실패: {} -> {}", artistName, e.getMessage());
            return new ArrayList<>();
        }
    }

    //Spotify URL 가져오기

    public String getSpotifyUrl(String artist, String title) {
        try {
            ensureValidToken();

            String query = artist + " " + title;

            SearchTracksRequest searchTracksRequest = spotifyApi
                    .searchTracks(query)
                    .limit(1)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            if (trackPaging.getItems().length > 0) {
                Track track = trackPaging.getItems()[0];
                String spotifyUrl = track.getExternalUrls().get("spotify");
                log.info("Spotify URL: {} - {} -> {}", artist, title, spotifyUrl);
                return spotifyUrl;
            }

            log.warn("Spotify URL 없음: {} - {}", artist, title);
            return null;

        } catch (Exception e) {
            log.error("URL 가져오기 실패: {} - {} -> {}", artist, title, e.getMessage());
            return null;
        }
    }

    //Track ID로 상세 정보 조회

    public SpotifyTrackDto getTrackById(String trackId) {
        try {
            ensureValidToken();

            GetTrackRequest getTrackRequest = spotifyApi.getTrack(trackId).build();
            Track track = getTrackRequest.execute();

            return convertToDto(track);

        } catch (Exception e) {
            log.error("Track 조회 실패: {} -> {}", trackId, e.getMessage());
            return null;
        }
    }

    //아티스트와 곡명으로 Song 엔티티 자동 완성
    public Song enrichSongFromSpotify(String artist, String title) {
        try {
            ensureValidToken();

            String query = artist + " " + title;

            SearchTracksRequest searchTracksRequest = spotifyApi
                    .searchTracks(query)
                    .limit(1)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            if (trackPaging.getItems().length > 0) {
                Track track = trackPaging.getItems()[0];

                Song song = new Song();
                song.setArtist(artist);
                song.setTitle(title);

                // Spotify URL
                song.setSpotifyUrl(track.getExternalUrls().get("spotify"));

                // 앨범 정보
                AlbumSimplified album = track.getAlbum();
                if (album != null) {
                    song.setAlbum(album.getName());
                    song.setYearReleased(extractYear(album.getReleaseDate()));
                }

                // 재생시간
                song.setDuration(track.getDurationMs() / 1000);

                // 장르 추론
                song.setGenre(inferGenreFromArtist(artist));

                // 카테고리 자동 분류
                song.setCategory(categorizeByArtist(artist));

                log.info("Song 완성: {} - {} ({}년, {}초)",
                        artist, title, song.getYearReleased(), song.getDuration());

                return song;
            } else {
                log.warn("Spotify에서 찾을 수 없음: {} - {}", artist, title);
                return createBasicSong(artist, title);
            }
        } catch (Exception e) {
            log.error("Song 완성 실패: {} - {} -> {}", artist, title, e.getMessage());
            return createBasicSong(artist, title);
        }
    }

    //Track을 DTO로 변환
    private SpotifyTrackDto convertToDto(Track track) {
        String artistName = track.getArtists().length > 0 ?
                track.getArtists()[0].getName() : "Unknown";

        String albumName = track.getAlbum() != null ?
                track.getAlbum().getName() : "Unknown";

        Integer releaseYear = track.getAlbum() != null ?
                extractYear(track.getAlbum().getReleaseDate()) : null;

//        String imageUrl = null;
//        if (track.getAlbum() != null && track.getAlbum().getImages().length > 0) {
//            imageUrl = track.getAlbum().getImages()[0].getUrl();
//        }

        return SpotifyTrackDto.builder()
                .id(track.getId())
                .name(track.getName())
                .artist(artistName)
                .album(albumName)
                .releaseYear(releaseYear)
                .durationMs(track.getDurationMs())
                .spotifyUrl(track.getExternalUrls().get("spotify"))
                .previewUrl(track.getPreviewUrl())
                .imageUrl(track.getAlbum().getImages()[0].getUrl())
                .popularity(track.getPopularity())
                .build();
    }

//발매일에서 연도 추출
    private Integer extractYear(String releaseDate) {
        if (releaseDate != null && releaseDate.length() >= 4) {
            try {
                return Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

//아티스트 기반 장르 추론

    private String inferGenreFromArtist(String artist) {
        if (isKoreanArtist(artist)) {
            return "K-Pop";
        }
        return "Pop";
    }

//아티스트 기반 카테고리 분류

    private String categorizeByArtist(String artist) {
        if (isKoreanArtist(artist)) {
            return "K-POP";
        }
        return "POP";
    }

    private boolean isKoreanArtist(String artist) {
        String[] koreanArtists = {
                "DAY6", "INFINITE", "Davichi", "다비치", "아이유", "IU",
                "BTS", "방탄소년단", "블랙핑크", "BLACKPINK", "트와이스", "TWICE"
        };

        for (String korean : koreanArtists) {
            if (artist.equalsIgnoreCase(korean)) {
                return true;
            }
        }

        return artist.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }

//기본 Song 객체 생성

    private Song createBasicSong(String artist, String title) {
        Song song = new Song();
        song.setArtist(artist);
        song.setTitle(title);
        song.setGenre(inferGenreFromArtist(artist));
        song.setCategory(categorizeByArtist(artist));
        return song;
    }

    public List<SpotifyTrackDto> searchTracksUnified(String query) {
        try {
            ensureValidToken();

            SearchTracksRequest searchTracksRequest = spotifyApi
                    .searchTracks(query)
                    .limit(20)
                    .build();

            Paging<Track> trackPaging = searchTracksRequest.execute();

            List<SpotifyTrackDto> tracks = new ArrayList<>();
            for (Track track : trackPaging.getItems()) {
                tracks.add(convertToDto(track));
            }

            log.info("통합 검색 성공: {} ({}건 발견)", query, tracks.size());
            return tracks;

        } catch (Exception e) {
            log.error("통합 검색 실패: {} -> {}", query, e.getMessage());
            return new ArrayList<>();
        }
    }
}