package kr.ac.kopo.repository;

import kr.ac.kopo.entity.Playlist;
import kr.ac.kopo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 사용자의 모든 재생목록
    List<Playlist> findByUserOrderByAddedAtDesc(User user);

    // 사용자의 즐겨찾기 목록
    List<Playlist> findByUserAndIsFavoriteTrueOrderByAddedAtDesc(User user);

    // 특정 곡이 사용자 재생목록에 있는지 확인
    Optional<Playlist> findByUserAndSongId(User user, Long songId);

    // 사용자가 재생목록에 추가한 곡 개수
    Long countByUser(User user);

    // 재생 횟수 기준 인기 곡 (전체 사용자)
    @Query("SELECT p.song, COUNT(p) as playCount FROM Playlist p " +
            "GROUP BY p.song ORDER BY playCount DESC")
    List<Object[]> findPopularSongs();
}
