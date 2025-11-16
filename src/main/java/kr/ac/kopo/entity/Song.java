package kr.ac.kopo.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "songs")
@Data
public class Song {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "artist", nullable = false, length = 100)
    private String artist;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "genre", length = 50)
    private String genre;

    @Column(name = "year_released")
    private Integer yearReleased;

    @Column(name = "album", length = 200)
    private String album;

    @Column(name = "duration")
    private Integer duration; // in seconds

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "category", length = 50)
    private String category; // "K-POP", "POP", "ROCK", etc.

    @Column(name = "image_url")
    private String imageUrl;
    // Default constructor
    public Song() {}

    // Constructor
    public Song(String artist, String title, String genre, Integer yearReleased,
                String album, Integer duration, String youtubeUrl, String spotifyUrl, String category) {
        this.artist = artist;
        this.title = title;
        this.genre = genre;
        this.yearReleased = yearReleased;
        this.album = album;
        this.duration = duration;
        this.youtubeUrl = youtubeUrl;
        this.spotifyUrl = spotifyUrl;
        this.category = category;
        this.imageUrl = getImageUrl();
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", yearReleased=" + yearReleased +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", youtubeUrl='" + youtubeUrl + '\'' +
                ", spotifyUrl='" + spotifyUrl + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}