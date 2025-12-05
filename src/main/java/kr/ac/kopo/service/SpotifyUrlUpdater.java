package kr.ac.kopo.service;

import kr.ac.kopo.entity.Song;
import kr.ac.kopo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyUrlUpdater {

    private final SongRepository songRepository;

    @Autowired(required = false)
    private SpotifyService spotifyService;

//모든 곡의 Spotify URL 업데이트

    public void updateAllSpotifyUrls() {
        if (spotifyService == null) {
            log.warn("SpotifyService가 비활성화되어 있습니다.");
            return;
        }

        List<Song> songs = songRepository.findAll();
        int updated = 0;
        int failed = 0;

        log.info("Spotify URL 업데이트 시작... (총 {}곡)", songs.size());

        for (Song song : songs) {
            // 이미 Spotify URL이 있으면 스킵
            if (song.getSpotifyUrl() != null && !song.getSpotifyUrl().isEmpty()) {
                log.debug("스킵 (이미 존재): {} - {}", song.getArtist(), song.getTitle());
                continue;
            }

            try {
                String spotifyUrl = spotifyService.getSpotifyUrl(song.getArtist(), song.getTitle());

                if (spotifyUrl != null) {
                    song.setSpotifyUrl(spotifyUrl);
                    songRepository.save(song);
                    updated++;
                    log.info("업데이트: {} - {} -> {}",
                            song.getArtist(), song.getTitle(), spotifyUrl);
                } else {
                    failed++;
                    log.warn("URL 없음: {} - {}", song.getArtist(), song.getTitle());
                }

                // API 호출 제한 방지
                Thread.sleep(100);

            } catch (Exception e) {
                failed++;
                log.error("실패: {} - {} -> {}",
                        song.getArtist(), song.getTitle(), e.getMessage());
            }
        }

        log.info("Spotify URL 업데이트 완료! (성공: {}, 실패: {})", updated, failed);
    }

    //전체 정보 업데이트 (앨범, 년도, 재생시간 등)

    public void updateAllSongInfo() {
        if (spotifyService == null) {
            log.warn("⚠️ SpotifyService가 비활성화되어 있습니다.");
            return;
        }

        List<Song> songs = songRepository.findAll();
        int updated = 0;

        log.info("전체 곡 정보 업데이트 시작... (총 {}곡)", songs.size());

        for (Song song : songs) {
            try {
                Song enrichedSong = spotifyService.enrichSongFromSpotify(
                        song.getArtist(),
                        song.getTitle()
                );

                // 기존 ID 유지하면서 정보 업데이트
                enrichedSong.setId(song.getId());
                songRepository.save(enrichedSong);
                updated++;

                log.info("업데이트: {} - {} (앨범: {}, {}년)",
                        song.getArtist(), song.getTitle(),
                        enrichedSong.getAlbum(), enrichedSong.getYearReleased());

                Thread.sleep(100);

            } catch (Exception e) {
                log.error("실패: {} - {} -> {}",
                        song.getArtist(), song.getTitle(), e.getMessage());
            }
        }

        log.info("전체 정보 업데이트 완료! ({}곡)", updated);
    }
}
