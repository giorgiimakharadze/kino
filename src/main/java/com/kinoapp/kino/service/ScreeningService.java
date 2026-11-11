package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.ScreeningForm;
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
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final BookingRepository bookingRepository;
    private final MovieService movieService;

    public ScreeningService(ScreeningRepository screeningRepository, BookingRepository bookingRepository,
                            MovieService movieService) {
        this.screeningRepository = screeningRepository;
        this.bookingRepository = bookingRepository;
        this.movieService = movieService;
    }

    @Transactional(readOnly = true)
    public List<Screening> upcomingForMovie(Long movieId) {
        return screeningRepository.findUpcomingByMovie(movieId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Screening get(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));
    }

    @Transactional
    public Screening create(ScreeningForm form) {
        Screening s = new Screening();
        s.setMovie(movieService.get(form.getMovieId()));
        s.setStartTime(form.getStartTime());
        s.setHall(form.getHall());
        s.setTotalSeats(form.getTotalSeats());
        s.setPrice(form.getPrice());
        return screeningRepository.save(s);
    }

    @Transactional
    public void delete(Long id) {
        Screening s = get(id);
        if (bookingRepository.existsByScreening_Id(id)) {
            throw new IllegalStateException("Cannot delete a screening that has bookings");
        }
        screeningRepository.delete(s);
    }
}
