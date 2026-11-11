package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.BookingForm;
import com.kinoapp.kino.entity.Booking;
import com.kinoapp.kino.entity.Screening;
import com.kinoapp.kino.repository.BookingRepository;
import com.kinoapp.kino.repository.ScreeningRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScreeningRepository screeningRepository;

    public BookingService(BookingRepository bookingRepository, ScreeningRepository screeningRepository) {
        this.bookingRepository = bookingRepository;
        this.screeningRepository = screeningRepository;
    }

    @Transactional
    public Booking book(BookingForm form) {
        // Lock the screening row so two people cannot take the last seat at once
        Screening screening = screeningRepository.findByIdForUpdate(form.getScreeningId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));

        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This screening has already started");
        }
        int free = screening.getTotalSeats() - bookingRepository.countBookedSeats(screening.getId());
        if (form.getSeats() > free) {
            throw new IllegalStateException("Only " + free + " seat(s) left");
        }

        Booking booking = new Booking();
        booking.setScreening(screening);
        booking.setCustomerName(form.getCustomerName());
        booking.setCustomerEmail(form.getCustomerEmail());
        booking.setSeats(form.getSeats());
        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancel(Long id, String email) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        // Wrong email gets the same 404, so ids cannot be probed
        if (!booking.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        bookingRepository.delete(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> findByEmail(String email) {
        return bookingRepository.findByCustomerEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public int freeSeats(Screening screening) {
        return screening.getTotalSeats() - bookingRepository.countBookedSeats(screening.getId());
    }
}
