package kr.ac.kopo.service;

import kr.ac.kopo.entity.Playlist;
import kr.ac.kopo.entity.Song;
import kr.ac.kopo.entity.User;
import kr.ac.kopo.repository.PlaylistRepository;
import kr.ac.kopo.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    /**
     * 재생목록에 곡 추가
     */
    public Playlist addToPlaylist(User user, Long songId) {
        // 이미 있는지 확인
        Optional<Playlist> existing = playlistRepository.findByUserAndSongId(user, songId);
        if (existing.isPresent()) {
            log.info("이미 재생목록에 있음: {}", songId);
            return existing.get();
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("곡을 찾을 수 없습니다: " + songId));

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setSong(song);

        return playlistRepository.save(playlist);
    }

    /**
     * 재생목록에서 제거
     */
    public void removeFromPlaylist(User user, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("재생목록 항목을 찾을 수 없습니다"));

        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다");
        }

        playlistRepository.delete(playlist);
    }

    /**
     * 사용자의 재생목록 가져오기
     */
    public List<Playlist> getUserPlaylist(User user) {
        return playlistRepository.findByUserOrderByAddedAtDesc(user);
    }

    /**
     * 즐겨찾기 토글
     */
    public Playlist toggleFavorite(User user, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("재생목록 항목을 찾을 수 없습니다"));

        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다");
        }

        playlist.setIsFavorite(!playlist.getIsFavorite());
        return playlistRepository.save(playlist);
    }

    /**
     * 재생 횟수 증가
     */
    public void incrementPlayCount(Long playlistId) {
        playlistRepository.findById(playlistId).ifPresent(playlist -> {
            playlist.setPlayCount(playlist.getPlayCount() + 1);
            playlistRepository.save(playlist);
        });
    }

    /**
     * 추천 곡 (사용자가 듣지 않은 인기 곡)
     */
    public List<Song> getRecommendedSongs(User user, int limit) {
        // 사용자의 재생목록에 있는 곡 ID 가져오기
        List<Long> userSongIds = getUserPlaylist(user).stream()
                .map(p -> p.getSong().getId())
                .collect(Collectors.toList());

        // 사용자가 듣지 않은 랜덤 곡 추천
        return songRepository.findRandomSongs(limit + userSongIds.size()).stream()
                .filter(song -> !userSongIds.contains(song.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 재생목록 개수
     */
    public Long getPlaylistCount(User user) {
        return playlistRepository.countByUser(user);
    }
}