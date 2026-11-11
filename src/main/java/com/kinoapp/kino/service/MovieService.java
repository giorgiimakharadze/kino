package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.MovieForm;
import com.kinoapp.kino.entity.Genre;
import com.kinoapp.kino.entity.Movie;
import com.kinoapp.kino.repository.BookingRepository;
import com.kinoapp.kino.repository.GenreRepository;
import com.kinoapp.kino.repository.MovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorage;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository,
                        BookingRepository bookingRepository, FileStorageService fileStorage) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.bookingRepository = bookingRepository;
        this.fileStorage = fileStorage;
    }

    // Paged and sorted list. optional title search and genre filter
    @Transactional(readOnly = true)
    public Page<Movie> search(String title, String genre, Pageable pageable) {
        String t = title == null ? "" : title;
        if (genre == null || genre.isBlank()) {
            return movieRepository.findByTitleContainingIgnoreCase(t, pageable);
        }
        return movieRepository.findByGenres_NameAndTitleContainingIgnoreCase(genre, t, pageable);
    }

    @Transactional(readOnly = true)
    public Movie get(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    @Transactional(readOnly = true)
    public List<Genre> allGenres() {
        return genreRepository.findAll();
    }

    @Transactional
    public Movie create(MovieForm form) {
        Movie movie = new Movie();
        apply(movie, form);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie update(Long id, MovieForm form) {
        Movie movie = get(id);
        apply(movie, form);
        return movieRepository.save(movie);
    }

    @Transactional
    public void delete(Long id) {
        Movie movie = get(id);
        // Never silently wipe customers' tickets
        if (bookingRepository.existsByScreening_Movie_Id(id)) {
            throw new IllegalStateException("Cannot delete a movie that has bookings");
        }
        movieRepository.delete(movie);
        fileStorage.delete(movie.getPosterPath());
    }

    // Copies form values onto the entity
    private void apply(Movie movie, MovieForm form) {
        movie.setTitle(form.getTitle());
        movie.setDescription(form.getDescription());
        movie.setDurationMinutes(form.getDurationMinutes());
        movie.setReleaseDate(form.getReleaseDate());

        Set<Genre> genres = new HashSet<>();
        if (form.getGenreIds() != null) {
            genres.addAll(genreRepository.findAllById(form.getGenreIds()));
        }
        movie.setGenres(genres);

        if (form.getPoster() != null && !form.getPoster().isEmpty()) {
            String newName = fileStorage.store(form.getPoster());
            fileStorage.delete(movie.getPosterPath());
            movie.setPosterPath(newName);
        }
    }
}
