package com.kinoapp.kino.repository;

import com.kinoapp.kino.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Method-name query: title search, paged and sorted by the Pageable
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Method-name query: filter by genre name and title together
    Page<Movie> findByGenres_NameAndTitleContainingIgnoreCase(String genre, String title, Pageable pageable);
}
