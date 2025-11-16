package kr.ac.kopo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "playlist")
@Data
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "play_count")
    private Integer playCount = 0;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}