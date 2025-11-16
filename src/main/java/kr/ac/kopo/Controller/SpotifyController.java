package kr.ac.kopo.Controller;


import kr.ac.kopo.dto.SpotifyTrackDto;
import kr.ac.kopo.entity.Song;
import kr.ac.kopo.service.SongService;
import kr.ac.kopo.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    @Autowired(required = false)
    private final SongService songService;

    private final SpotifyService spotifyService;
    /**
     * Spotify에서 곡 검색
     */
    @GetMapping("/search-spotify")
    public ResponseEntity<?> searchSpotify(@RequestParam String query) {

        if (spotifyService == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Spotify 서비스가 활성화되지 않았습니다. application.properties를 확인하세요.");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // 통합 검색 메서드 사용
            List<SpotifyTrackDto> tracks = spotifyService.searchTracksUnified(query);
            return ResponseEntity.ok(tracks);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "검색 중 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Spotify에서 검색한 곡을 DB에 추가
     */
    @PostMapping("/add-from-spotify")
    public ResponseEntity<Map<String, Object>> addFromSpotify(
            @RequestBody SpotifyTrackDto trackDto) {

        Map<String, Object> response = new HashMap<>();

        try {
            // SpotifyTrackDto를 Song 엔티티로 변환
            Song song = new Song();
            song.setArtist(trackDto.getArtist());
            song.setTitle(trackDto.getName());
            song.setAlbum(trackDto.getAlbum());
            song.setYearReleased(trackDto.getReleaseYear());
            song.setDuration(trackDto.getDurationMs() / 1000); // 밀리초 → 초
            song.setSpotifyUrl(trackDto.getSpotifyUrl());
            song.setGenre(inferGenre(trackDto.getArtist()));
            song.setCategory(inferCategory(trackDto.getArtist()));

            // DB에 저장
            Song savedSong = songService.saveSong(song);

            response.put("success", true);
            response.put("message", "곡이 성공적으로 추가되었습니다!");
            response.put("song", savedSong);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "곡 추가 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 장르 추론
     */
    private String inferGenre(String artist) {
        if (isKoreanArtist(artist)) {
            return "K-Pop";
        }
        return "Pop";
    }

    /**
     * 카테고리 추론
     */
    private String inferCategory(String artist) {
        if (isKoreanArtist(artist)) {
            return "K-POP";
        }
        return "POP";
    }

    /**
     * 한국 아티스트 여부 확인
     */
    private boolean isKoreanArtist(String artist) {
        return artist.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
    /**
     * 곡 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<SpotifyTrackDto>> searchTracks(
            @RequestParam String artist,
            @RequestParam String title) {

        List<SpotifyTrackDto> tracks = spotifyService.searchTracks(artist, title);
        return ResponseEntity.ok(tracks);
    }

    /**
     * 아티스트로 검색
     */
    @GetMapping("/artist/{artistName}")
    public ResponseEntity<List<SpotifyTrackDto>> searchByArtist(
            @PathVariable String artistName) {

        List<SpotifyTrackDto> tracks = spotifyService.searchByArtist(artistName);
        return ResponseEntity.ok(tracks);
    }

    /**
     * Spotify URL 가져오기
     */
    @GetMapping("/url")
    public ResponseEntity<String> getSpotifyUrl(
            @RequestParam String artist,
            @RequestParam String title) {

        String url = spotifyService.getSpotifyUrl(artist, title);
        if (url != null) {
            return ResponseEntity.ok(url);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Track ID로 상세 정보 조회
     */
    @GetMapping("/track/{trackId}")
    public ResponseEntity<SpotifyTrackDto> getTrack(@PathVariable String trackId) {
        SpotifyTrackDto track = spotifyService.getTrackById(trackId);
        if (track != null) {
            return ResponseEntity.ok(track);
        }
        return ResponseEntity.notFound().build();
    }
}