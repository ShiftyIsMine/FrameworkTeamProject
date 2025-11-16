//package kr.ac.kopo.service;
//
//
//import kr.ac.kopo.dto.SpotifyTrackDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.hc.core5.http.ParseException;
//import org.springframework.stereotype.Service;
//import se.michaelthelin.spotify.SpotifyApi;
//import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
//import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
//import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
//import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
//import se.michaelthelin.spotify.model_objects.specification.Paging;
//import se.michaelthelin.spotify.model_objects.specification.Track;
//import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
//import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
//import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SpotifyServiceold {
//
//    private final SpotifyApi spotifyApi;
//    private String accessToken;
//
//    /**
//     * Access Token 발급 (Client Credentials Flow)
//     * - 사용자 정보 접근 없이 공개 데이터만 접근
//     */
//    public void getAccessToken() {
//        try {
//            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
//            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
//
//            accessToken = clientCredentials.getAccessToken();
//            spotifyApi.setAccessToken(accessToken);
//
//            log.info("Access Token 발급 성공. 만료 시간: {} 초", clientCredentials.getExpiresIn());
//
//        } catch (IOException | SpotifyWebApiException | ParseException e) {
//            log.error("Access Token 발급 실패: {}", e.getMessage());
//        }
//    }
//
//    /**
//     * 토큰 갱신 확인
//     */
//    private void ensureAccessToken() {
//        if (accessToken == null || accessToken.isEmpty()) {
//            getAccessToken();
//        }
//    }
//
//    /**
//     * 곡 검색 (아티스트명 + 곡명)
//     */
//    public List<SpotifyTrackDto> searchTracks(String artist, String title) {
//        ensureAccessToken();
//
//        try {
//            String query = artist + " " + title;
//            SearchTracksRequest searchRequest = spotifyApi.searchTracks(query)
//                    .limit(10)
//                    .build();
//
//            Paging<Track> trackPaging = searchRequest.execute();
//            Track[] tracks = trackPaging.getItems();
//
//            List<SpotifyTrackDto> results = new ArrayList<>();
//            for (Track track : tracks) {
//                results.add(convertToDto(track));
//            }
//
//            return results;
//
//        } catch (IOException | SpotifyWebApiException | ParseException e) {
//            log.error("곡 검색 실패: {}", e.getMessage());
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * Track ID로 곡 정보 가져오기
//     */
//    public SpotifyTrackDto getTrackById(String trackId) {
//        ensureAccessToken();
//
//        try {
//            GetTrackRequest getTrackRequest = spotifyApi.getTrack(trackId).build();
//            Track track = getTrackRequest.execute();
//
//            return convertToDto(track);
//
//        } catch (IOException | SpotifyWebApiException | ParseException e) {
//            log.error("Track 정보 가져오기 실패: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * 아티스트별 곡 검색
//     */
//    public List<SpotifyTrackDto> searchByArtist(String artistName) {
//        ensureAccessToken();
//
//        try {
//            SearchTracksRequest searchRequest = spotifyApi.searchTracks("artist:" + artistName)
//                    .limit(20)
//                    .build();
//
//            Paging<Track> trackPaging = searchRequest.execute();
//            Track[] tracks = trackPaging.getItems();
//
//            List<SpotifyTrackDto> results = new ArrayList<>();
//            for (Track track : tracks) {
//                results.add(convertToDto(track));
//            }
//
//            return results;
//
//        } catch (IOException | SpotifyWebApiException | ParseException e) {
//            log.error("아티스트 검색 실패: {}", e.getMessage());
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * Spotify URL 추출 (곡 링크)
//     */
//    public String getSpotifyUrl(String artist, String title) {
//        List<SpotifyTrackDto> tracks = searchTracks(artist, title);
//
//        if (!tracks.isEmpty()) {
//            return tracks.get(0).getSpotifyUrl();
//        }
//
//        return null;
//    }
//
//    /**
//     * Track 객체를 DTO로 변환
//     */
//    private SpotifyTrackDto convertToDto(Track track) {
//        SpotifyTrackDto dto = new SpotifyTrackDto();
//        dto.setTrackId(track.getId());
//        dto.setTitle(track.getName());
//        dto.setSpotifyUrl(track.getExternalUrls().get("spotify"));
//        dto.setPreviewUrl(track.getPreviewUrl()); // 30초 미리듣기 URL
//        dto.setDurationMs(track.getDurationMs());
//        dto.setPopularity(track.getPopularity());
//
//        // 앨범 정보
//        AlbumSimplified album = track.getAlbum();
//        dto.setAlbumName(album.getName());
//        dto.setAlbumImageUrl(album.getImages().length > 0 ? dto.getAlbumImageUrl() : null);
//        dto.setReleaseDate(album.getReleaseDate());
//
//        // 아티스트 정보
//        ArtistSimplified[] artists = track.getArtists();
//        if (artists.length > 0) {
//            dto.setArtistName(artists[0].getName());
//            dto.setArtistId(artists[0].getId());
//        }
//
//        return dto;
//    }
//}