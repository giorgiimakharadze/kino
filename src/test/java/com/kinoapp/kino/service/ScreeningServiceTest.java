package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.BookingForm;
import com.kinoapp.kino.dto.ScreeningForm;
import com.kinoapp.kino.entity.Movie;
import com.kinoapp.kino.entity.Screening;
import com.kinoapp.kino.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Checks the data rules: no deleting a movie that has bookings, no double-booked halls
@DataJpaTest
@Import({ScreeningService.class, MovieService.class, BookingService.class, FileStorageService.class})
class ScreeningServiceTest {

    @Autowired
    private ScreeningService screeningService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private MovieRepository movieRepository;

    private Movie movie;
    // Whole minutes, like the browser's datetime field (the database cannot keep nanoseconds exactly)
    private final LocalDateTime start = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setTitle("Test movie");
        movieRepository.save(movie);
    }

    private ScreeningForm screeningForm(String hall) {
        ScreeningForm f = new ScreeningForm();
        f.setMovieId(movie.getId());
        f.setStartTime(start);
        f.setHall(hall);
        f.setTotalSeats(10);
        f.setPrice(BigDecimal.TEN);
        return f;
    }

    private void bookOneSeat(Screening screening) {
        BookingForm b = new BookingForm();
        b.setScreeningId(screening.getId());
        b.setCustomerName("Ann");
        b.setCustomerEmail("ann@example.com");
        b.setSeats(1);
        bookingService.book(b);
    }

    @Test
    void cannotDeleteMovieWithBookings() {
        Screening s = screeningService.create(screeningForm("Hall 1"));
        bookOneSeat(s);
        assertThrows(IllegalStateException.class, () -> movieService.delete(movie.getId()));
    }

    @Test
    void sameHallAtSameTimeIsRejected() {
        assertDoesNotThrow(() -> screeningService.create(screeningForm("Hall 1")));
        assertThrows(IllegalStateException.class, () -> screeningService.create(screeningForm("Hall 1")));
        // A different hall at that time is fine
        assertDoesNotThrow(() -> screeningService.create(screeningForm("Hall 2")));
    }
}
