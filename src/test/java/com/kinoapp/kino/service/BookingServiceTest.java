package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.BookingForm;
import com.kinoapp.kino.entity.Booking;
import com.kinoapp.kino.entity.Movie;
import com.kinoapp.kino.entity.Screening;
import com.kinoapp.kino.repository.MovieRepository;
import com.kinoapp.kino.repository.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Runs against a real database, with only the booking service loaded
@DataJpaTest
@Import(BookingService.class)
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ScreeningRepository screeningRepository;

    private Screening screening;

    @BeforeEach
    void setUp() {
        Movie movie = new Movie();
        movie.setTitle("Test movie");
        movieRepository.save(movie);

        screening = new Screening();
        screening.setMovie(movie);
        screening.setStartTime(LocalDateTime.now().plusDays(1));
        screening.setHall("Hall 1");
        screening.setTotalSeats(2);
        screening.setPrice(BigDecimal.TEN);
        screeningRepository.save(screening);
    }

    private BookingForm form(int seats) {
        BookingForm f = new BookingForm();
        f.setScreeningId(screening.getId());
        f.setCustomerName("Ann");
        f.setCustomerEmail("ann@example.com");
        f.setSeats(seats);
        return f;
    }

    @Test
    void cannotBookMoreSeatsThanLeft() {
        assertDoesNotThrow(() -> bookingService.book(form(2)));
        // Screening has 2 seats and both are taken
        assertThrows(IllegalStateException.class, () -> bookingService.book(form(1)));
    }

    @Test
    void cannotBookPastScreening() {
        screening.setStartTime(LocalDateTime.now().minusHours(1));
        screeningRepository.save(screening);
        assertThrows(IllegalStateException.class, () -> bookingService.book(form(1)));
    }

    @Test
    void cancelNeedsMatchingEmail() {
        Booking booking = bookingService.book(form(1));
        assertThrows(ResponseStatusException.class, () -> bookingService.cancel(booking.getId(), "other@example.com"));
        assertDoesNotThrow(() -> bookingService.cancel(booking.getId(), "ANN@example.com"));
    }
}
