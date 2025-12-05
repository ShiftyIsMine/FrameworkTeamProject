package kr.ac.kopo.config;


import kr.ac.kopo.entity.Song;
import kr.ac.kopo.entity.User;
import kr.ac.kopo.service.SongService;
import kr.ac.kopo.service.SpotifyService;
import kr.ac.kopo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SongService songService;
    private final UserService userService;

    @Autowired(required = false)
    private SpotifyService spotifyService;

    public DataInitializer(SongService songService, @Lazy UserService userService) {
        this.songService = songService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize users
        if (userService.getTotalUserCount() == 0) {
            initializeUsers();
        }

        // Initialize songs
        if (songService.getTotalSongCount() == 0) {
            initializeSongs();
        }
    }

    private void initializeUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole(User.Role.valueOf("ADMIN"));
        userService.createUser(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword("user123");
        user.setRole(User.Role.valueOf("USER"));
        userService.createUser(user);

    }

    private void initializeSongs() {
        System.out.println("🎵 Spotify API로 곡 정보 자동 채우기 시작...");

//아티스트명과 곡명만 입력하면 끝!

        // DAY6
        addSong("DAY6", "한 페이지가 될 수 있게");
        addSong("DAY6", "I Loved You");
        addSong("DAY6", "Congratulations");
        addSong("DAY6", "Zombie");

        // INFINITE
        addSong("INFINITE", "그리움이 닿는 곳에");
        addSong("INFINITE", "파라다이스");
        addSong("INFINITE", "마주보며 서 있어");
        addSong("INFINITE", "Man In Love");

        // BBMAK
        addSong("BBMAK", "Back Here");
        addSong("BBMAK", "Ghost Of You And Me");
        addSong("BBMAK", "Miss You More");
        addSong("BBMAK", "Sympathy");

        // Taylor Swift
        addSong("Taylor Swift", "Enchanted");
        addSong("Taylor Swift", "Back To December");
        addSong("Taylor Swift", "Anti-Hero");
        addSong("Taylor Swift", "Fortnight");

        // One Direction
        addSong("One Direction", "Best Song Ever");
        addSong("One Direction", "Up All Night");
        addSong("One Direction", "Night Changes");
        addSong("One Direction", "What Makes You Beautiful");

        // Backstreet Boys
        addSong("Backstreet Boys", "Shape Of My Heart");
        addSong("Backstreet Boys", "Bigger");
        addSong("Backstreet Boys", "Lift Me Up");
        addSong("Backstreet Boys", "Don't Wanna Lose You Now");

        // Davichi
        addSong("Davichi", "녹는 중");
        addSong("Davichi", "너에게 못했던 내 마지막 말은");
        addSong("Davichi", "그냥 안아달란 말야");
        addSong("Davichi", "모르시나요");

        // Westlife
        addSong("Westlife", "Don't Let Me Go");
        addSong("Westlife", "Beautiful World");
        addSong("Westlife", "I Will Reach You");
        addSong("Westlife", "Something Right");

        System.out.println("✅ 모든 곡이 Spotify 정보와 함께 저장되었습니다!");
    }

//핵심 메서드: 아티스트와 곡명만으로 완전한 Song 저장

    private void addSong(String artist, String title) {
        if (spotifyService != null) {
            try {
                // Spotify API로 모든 정보 자동 채우기
                Song song = spotifyService.enrichSongFromSpotify(artist, title);
                song.setImageUrl(spotifyService.getTrackById(song.getAlbum()).getImageUrl());
//                // YouTube URL도 추가
//                if (song.getYoutubeUrl() == null) {
//                    song.setYoutubeUrl(spotifyService.searchYouTubeUrl(artist, title));
//                }

                songService.saveSong(song);

                System.out.printf("   %s - %s (앨범: %s, %d년, %d초)%n",
                        artist, title,
                        song.getAlbum() != null ? song.getAlbum() : "N/A",
                        song.getYearReleased() != null ? song.getYearReleased() : 0,
                        song.getDuration() != null ? song.getDuration() : 0);

            } catch (Exception e) {
                System.err.println(" 실패: " + artist + " - " + title + " (" + e.getMessage() + ")");

                // 실패해도 기본 정보로라도 저장
                Song basicSong = new Song();
                basicSong.setArtist(artist);
                basicSong.setTitle(title);
                basicSong.setGenre("Pop");
                basicSong.setCategory("POP");
                songService.saveSong(basicSong);
            }
        } else {
            // Spotify 서비스가 없으면 기본 정보만 저장
            Song basicSong = new Song();
            basicSong.setArtist(artist);
            basicSong.setTitle(title);
            basicSong.setGenre("Pop");
            basicSong.setCategory("POP");
            songService.saveSong(basicSong);
            System.out.println("Spotify 비활성화: " + artist + " - " + title);
        }

        // API 호출 제한 방지를 위한 대기
        try {
            Thread.sleep(100); // 100ms 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}