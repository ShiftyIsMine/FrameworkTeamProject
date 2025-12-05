package kr.ac.kopo.service;


import kr.ac.kopo.entity.Song;
import kr.ac.kopo.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SongService {

    @Autowired
    private SongRepository songRepository;

    // Spotify 서비스 (선택사항)
    @Autowired(required = false)
    private SpotifyService spotifyService;


    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }
    public List<Song> getAllSongsAsList() {
        return songRepository.findAll(Sort.by("artist").ascending());
    }

    public Page<Song> getAllSongs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return songRepository.findAll(pageable);
    }


    public Optional<Song> getSongById(Long id) {
        return songRepository.findById(id);
    }


    public Song saveSong(Song song) {
        songRepository.save(song);
        return song;
    }

//Update song
    public void updateSong(Long id, Song songDetails) {
        songRepository.findById(id)
                .map(song -> {
                    song.setArtist(songDetails.getArtist());
                    song.setTitle(songDetails.getTitle());
                    song.setGenre(songDetails.getGenre());
                    song.setYearReleased(songDetails.getYearReleased());
                    song.setAlbum(songDetails.getAlbum());
                    song.setDuration(songDetails.getDuration());
                    song.setYoutubeUrl(songDetails.getYoutubeUrl());
                    song.setSpotifyUrl(songDetails.getSpotifyUrl());
                    song.setCategory(songDetails.getCategory());
                    song.setImageUrl(songDetails.getImageUrl());
                    return songRepository.save(song);
                })
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));
    }

//Delete song

    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

//Search songs

    public List<Song> searchSongs(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllSongs();
        }
        return songRepository.searchByArtistOrTitle(searchTerm.trim());
    }

    //Search songs with pagination

    public Page<Song> searchSongs(String searchTerm, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return songRepository.findAll(pageable);
        }
        return songRepository.searchSongs(searchTerm.trim(), pageable);
    }

    //Get songs by artist

    public List<Song> getSongsByArtist(String artist) {
        return songRepository.findByArtistContainingIgnoreCase(artist);
    }

    //Get songs by category
    public List<Song> getSongsByCategory(String category) {
        return songRepository.findByCategory(category);
    }

    /**
     * Get songs by category with pagination
     */
    public Page<Song> getSongsByCategory(String category, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return songRepository.findByCategory(category, pageable);
    }

    //Get songs by genre

    public List<Song> getSongsByGenre(String genre) {
        return songRepository.findByGenre(genre);
    }

    // Get songs by year

    public List<Song> getSongsByYear(Integer year) {
        return songRepository.findByYearReleased(year);
    }

    //Get songs by year range

    public List<Song> getSongsByYearRange(Integer startYear, Integer endYear) {
        return songRepository.findByYearReleasedBetween(startYear, endYear);
    }


    public List<String> getAllCategories() {
        return songRepository.findAllCategories();
    }


    public List<String> getAllGenres() {
        return songRepository.findAllGenres();
    }

    public List<String> getAllArtists() {
        return songRepository.findAllArtists();
    }

    public List<Song> getRandomSongs(int limit) {
        return songRepository.findRandomSongs(limit);
    }


    public List<Song> getLatestSongs() {
        return songRepository.findTop10ByOrderByIdDesc();
    }


    public Long getSongCountByCategory(String category) {
        return songRepository.countByCategory(category);
    }


    public Long getSongCountByArtist(String artist) {
        return songRepository.countByArtistContainingIgnoreCase(artist);
    }


    public Long getTotalSongCount() {
        return songRepository.count();
    }
}
