package kr.ac.kopo.repository;


import kr.ac.kopo.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Find by artist
    List<Song> findByArtistContainingIgnoreCase(String artist);

    // Find by category
    List<Song> findByCategory(String category);
    Page<Song> findByCategory(String category, Pageable pageable);

    // Find by genre
    List<Song> findByGenre(String genre);

    // Find by year
    List<Song> findByYearReleased(Integer year);

    // Find by year range
    List<Song> findByYearReleasedBetween(Integer startYear, Integer endYear);

    // Search by artist or title (no pagination)
    @Query("SELECT s FROM Song s WHERE LOWER(s.artist) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Song> searchByArtistOrTitle(@Param("searchTerm") String searchTerm);

    // Search with pagination
    @Query("SELECT s FROM Song s WHERE LOWER(s.artist) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.album) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Song> searchSongs(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Get all categories
    @Query("SELECT DISTINCT s.category FROM Song s WHERE s.category IS NOT NULL ORDER BY s.category")
    List<String> findAllCategories();

    // Get all genres
    @Query("SELECT DISTINCT s.genre FROM Song s WHERE s.genre IS NOT NULL ORDER BY s.genre")
    List<String> findAllGenres();

    // Get all artists
    @Query("SELECT DISTINCT s.artist FROM Song s ORDER BY s.artist")
    List<String> findAllArtists();

    // Get random songs
    @Query(value = "SELECT * FROM songs ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Song> findRandomSongs(@Param("limit") int limit);

    // Get latest songs
    List<Song> findTop10ByOrderByIdDesc();

    // Count by category
    Long countByCategory(String category);

    // Count by artist
    Long countByArtistContainingIgnoreCase(String artist);
}