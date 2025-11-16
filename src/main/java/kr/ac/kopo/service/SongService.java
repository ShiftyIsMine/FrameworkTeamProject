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

    /**
     * Get all songs (no pagination)
     */
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }
    public List<Song> getAllSongsAsList() {
        return songRepository.findAll(Sort.by("artist").ascending());
    }

    /**
     * Get songs with pagination
     */
    public Page<Song> getAllSongs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return songRepository.findAll(pageable);
    }

    /**
     * Get song by ID
     */
    public Optional<Song> getSongById(Long id) {
        return songRepository.findById(id);
    }

    /**
     * Save song
     *
     * @return
     */
    public Song saveSong(Song song) {
        songRepository.save(song);
        return song;
    }

    /**
     * Update song
     */
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
                    return songRepository.save(song);
                })
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));
    }

    /**
     * Delete song
     */
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    /**
     * Search songs (no pagination)
     */
    public List<Song> searchSongs(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllSongs();
        }
        return songRepository.searchByArtistOrTitle(searchTerm.trim());
    }

    /**
     * Search songs with pagination
     */
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

    /**
     * Get songs by artist
     */
    public List<Song> getSongsByArtist(String artist) {
        return songRepository.findByArtistContainingIgnoreCase(artist);
    }

    /**
     * Get songs by category (no pagination)
     */
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

    /**
     * Get songs by genre
     */
    public List<Song> getSongsByGenre(String genre) {
        return songRepository.findByGenre(genre);
    }

    /**
     * Get songs by year
     */
    public List<Song> getSongsByYear(Integer year) {
        return songRepository.findByYearReleased(year);
    }

    /**
     * Get songs by year range
     */
    public List<Song> getSongsByYearRange(Integer startYear, Integer endYear) {
        return songRepository.findByYearReleasedBetween(startYear, endYear);
    }

    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return songRepository.findAllCategories();
    }

    /**
     * Get all genres
     */
    public List<String> getAllGenres() {
        return songRepository.findAllGenres();
    }

    /**
     * Get all artists
     */
    public List<String> getAllArtists() {
        return songRepository.findAllArtists();
    }

    /**
     * Get random songs
     */
    public List<Song> getRandomSongs(int limit) {
        return songRepository.findRandomSongs(limit);
    }

    /**
     * Get latest songs
     */
    public List<Song> getLatestSongs() {
        return songRepository.findTop10ByOrderByIdDesc();
    }

    /**
     * Get song count by category
     */
    public Long getSongCountByCategory(String category) {
        return songRepository.countByCategory(category);
    }

    /**
     * Get song count by artist
     */
    public Long getSongCountByArtist(String artist) {
        return songRepository.countByArtistContainingIgnoreCase(artist);
    }

    /**
     * Get total song count
     */
    public Long getTotalSongCount() {
        return songRepository.count();
    }
}
