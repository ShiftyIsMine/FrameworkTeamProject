package kr.ac.kopo.Controller;

import kr.ac.kopo.entity.Playlist;
import kr.ac.kopo.entity.Song;
import kr.ac.kopo.entity.User;
import kr.ac.kopo.service.PlaylistService;
import kr.ac.kopo.service.SongService;
import kr.ac.kopo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private SongService songService;

    @GetMapping("/")
    public String home() {

        return "assignment";
    }

    @GetMapping("/inner1")
    public String inner1(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "100") int size,
                         @RequestParam(required = false) String search) {

        System.out.println("=== /inner1 호출됨 ===");

        Page<Song> songsPage;

        if (search != null && !search.trim().isEmpty()) {
            songsPage = songService.searchSongs(search, page, size, "artist", "asc");
            model.addAttribute("search", search);
        } else {
            songsPage = songService.getAllSongs(page, size, "artist", "asc");
        }

        // 디버깅 로그
        System.out.println("Total songs: " + songService.getTotalSongCount());
        System.out.println("Page has content: " + songsPage.hasContent());
        System.out.println("Page size: " + songsPage.getContent().size());

        // ✅ 수정: LinkedHashMap으로 순서 유지하며 아티스트별로 그룹화
        Map<String, List<Song>> songsByArtist = songsPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        Song::getArtist,
                        LinkedHashMap::new,  // 순서 유지
                        Collectors.toList()
                ));

        model.addAttribute("songsPage", songsPage);
        model.addAttribute("songsByArtist", songsByArtist);
        model.addAttribute("totalSongs", songService.getTotalSongCount());

        return "assignmentInner1";
    }

    @GetMapping("/inner2")
    public String inner2(Model model, Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());

        // 사용자의 재생목록
        List<Playlist> myPlaylist = playlistService.getUserPlaylist(user);

        // 추천 곡 (10개)
        List<Song> recommendedSongs = playlistService.getRecommendedSongs(user, 10);

        List<Song> allSongs = songService.getAllSongsAsList();

        model.addAttribute("myPlaylist", myPlaylist);
        model.addAttribute("recommendedSongs", recommendedSongs);
        model.addAttribute("allSongs", allSongs);
        model.addAttribute("playlistCount", myPlaylist.size());

        return "assignmentInner2";
    }
    @GetMapping("/add-song")
    public String addSongPage(Model model) {
        return "add-song";
    }

    // REST API endpoints for AJAX requests
    @GetMapping("/api/songs")
    @ResponseBody
    public Page<Song> getSongs(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               @RequestParam(defaultValue = "artist") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String category) {

        if (search != null && !search.trim().isEmpty()) {
            return songService.searchSongs(search, page, size, sortBy, sortDir);
        } else if (category != null && !category.trim().isEmpty()) {
            return songService.getSongsByCategory(category, page, size, sortBy, sortDir);
        } else {
            return songService.getAllSongs(page, size, sortBy, sortDir);
        }
    }


    //지워도 되는 부분(수동으로 곡 추가했던 버전)
    @GetMapping("/api/categories")
    @ResponseBody
    public List<String> getCategories() {
        return songService.getAllCategories();
    }

    @GetMapping("/api/artists")
    @ResponseBody
    public List<String> getArtists() {
        return songService.getAllArtists();
    }

    @GetMapping("/api/genres")
    @ResponseBody
    public List<String> getGenres() {
        return songService.getAllGenres();
    }

    @GetMapping("/api/songs/random/{limit}")
    @ResponseBody
    public List<Song> getRandomSongs(@PathVariable int limit) {
        return songService.getRandomSongs(limit);
    }

    @GetMapping("/api/songs/latest")
    @ResponseBody
    public List<Song> getLatestSongs() {
        return songService.getLatestSongs();
    }
}