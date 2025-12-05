package kr.ac.kopo.Controller;

import kr.ac.kopo.entity.Playlist;
import kr.ac.kopo.entity.User;
import kr.ac.kopo.service.PlaylistService;
import kr.ac.kopo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final UserService userService;

//재생목록에 추가

    @PostMapping("/add/{songId}")
    public ResponseEntity<Map<String, Object>> addToPlaylist(
            @PathVariable Long songId,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        Playlist playlist = playlistService.addToPlaylist(user, songId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "재생목록에 추가되었습니다");
        response.put("playlistId", playlist.getId());

        return ResponseEntity.ok(response);
    }

//재생목록에서 제거

    @DeleteMapping("/remove/{playlistId}")
    public ResponseEntity<Map<String, Object>> removeFromPlaylist(
            @PathVariable Long playlistId,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        playlistService.removeFromPlaylist(user, playlistId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "재생목록에서 제거되었습니다");

        return ResponseEntity.ok(response);
    }

//즐겨찾기 토글

    @PostMapping("/favorite/{playlistId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long playlistId,
            Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        Playlist playlist = playlistService.toggleFavorite(user, playlistId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFavorite", playlist.getIsFavorite());

        return ResponseEntity.ok(response);
    }

//재생 횟수 증가

    @PostMapping("/play/{playlistId}")
    public ResponseEntity<Void> incrementPlayCount(@PathVariable Long playlistId) {
        playlistService.incrementPlayCount(playlistId);
        return ResponseEntity.ok().build();
    }
}