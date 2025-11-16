package kr.ac.kopo.Controller;


import kr.ac.kopo.entity.Song;
import kr.ac.kopo.entity.User;
import kr.ac.kopo.service.SongService;
import kr.ac.kopo.service.SpotifyUrlUpdater;
import kr.ac.kopo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private SongService songService;

    @Autowired
    private UserService userService;

    public AdminController(SpotifyUrlUpdater spotifyUrlUpdater) {
        this.spotifyUrlUpdater = spotifyUrlUpdater;
    }

    // Admin Dashboard
    @GetMapping("/")
    public String adminDashboard(Model model) {
        // Get statistics
        Long totalSongs = songService.getTotalSongCount();
        Long totalUsers = userService.getTotalUserCount();
        Long activeUsers = userService.getActiveUserCount();
        List<String> categories = songService.getAllCategories();

        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("categories", categories);

        // Get recent songs and users
        List<Song> latestSongs = songService.getLatestSongs();
        model.addAttribute("latestSongs", latestSongs);

        return "admin/dashboard";
    }

    // Song Management
    @GetMapping("/songs")
    public String manageSongs(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "desc") String sortDir,
                              @RequestParam(required = false) String search) {

        Page<Song> songsPage;

        if (search != null && !search.trim().isEmpty()) {
            songsPage = songService.searchSongs(search, page, size, sortBy, sortDir);
            model.addAttribute("search", search);
        } else {
            songsPage = songService.getAllSongs(page, size, sortBy, sortDir);
        }

        model.addAttribute("songsPage", songsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "admin/songs";
    }

    @GetMapping("/songs/add")
    public String addSongForm(Model model) {
        model.addAttribute("song", new Song());
        model.addAttribute("categories", songService.getAllCategories());
        model.addAttribute("genres", songService.getAllGenres());
        return "admin/song-form";
    }

    @PostMapping("/songs/add")
    public String addSong(@Valid @ModelAttribute Song song,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", songService.getAllCategories());
            model.addAttribute("genres", songService.getAllGenres());
            return "admin/song-form";
        }

        try {
            songService.saveSong(song);
            redirectAttributes.addFlashAttribute("successMessage", "곡이 성공적으로 추가되었습니다.");
            return "redirect:/admin/songs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "곡 추가 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("categories", songService.getAllCategories());
            model.addAttribute("genres", songService.getAllGenres());
            return "admin/song-form";
        }
    }

    @GetMapping("/songs/edit/{id}")
    public String editSongForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return songService.getSongById(id)
                .map(song -> {
                    model.addAttribute("song", song);
                    model.addAttribute("categories", songService.getAllCategories());
                    model.addAttribute("genres", songService.getAllGenres());
                    return "admin/song-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "곡을 찾을 수 없습니다.");
                    return "redirect:/admin/songs";
                });
    }

    @PostMapping("/songs/edit/{id}")
    public String editSong(@PathVariable Long id,
                           @Valid @ModelAttribute Song song,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", songService.getAllCategories());
            model.addAttribute("genres", songService.getAllGenres());
            return "admin/song-form";
        }

        try {
            songService.updateSong(id, song);
            redirectAttributes.addFlashAttribute("successMessage", "곡이 성공적으로 수정되었습니다.");
            return "redirect:/admin/songs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "곡 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("categories", songService.getAllCategories());
            model.addAttribute("genres", songService.getAllGenres());
            return "admin/song-form";
        }
    }

    @PostMapping("/songs/delete/{id}")
    public String deleteSong(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            songService.deleteSong(id);
            redirectAttributes.addFlashAttribute("successMessage", "곡이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "곡 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/songs";
    }

    // User Management
    @GetMapping("/users")
    public String manageUsers(Model model,
                              @RequestParam(required = false) String search) {

        List<User> users;

        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
            model.addAttribute("search", search);
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);

        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "사용자 상태가 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    private final SpotifyUrlUpdater spotifyUrlUpdater;

    /**
     * Spotify URL 일괄 업데이트 (관리자 전용)
     */
    @RequestMapping(value = "/update-spotify-urls", method = {RequestMethod.GET, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSpotifyUrls() {
        try {
            spotifyUrlUpdater.updateAllSpotifyUrls();
            return ResponseEntity.ok("✅ Spotify URL 업데이트 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 전체 곡 정보 일괄 업데이트
     */
    @RequestMapping(value = "/update-all-info", method = {RequestMethod.GET, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAllInfo() {
        try {
            spotifyUrlUpdater.updateAllSongInfo();
            return ResponseEntity.ok("✅ 전체 정보 업데이트 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ 업데이트 실패: " + e.getMessage());
        }
    }
}