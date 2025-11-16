package kr.ac.kopo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyTrackDto {
    private String id;
    private String name;
    private String artist;
    private String album;
    private Integer releaseYear;
    private Integer durationMs;
    private String spotifyUrl;
    private String previewUrl;
    private String imageUrl;
    private Integer popularity;
}