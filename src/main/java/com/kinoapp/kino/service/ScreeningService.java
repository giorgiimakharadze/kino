package com.kinoapp.kino.service;

import com.kinoapp.kino.dto.ScreeningForm;
import com.kinoapp.kino.entity.Screening;
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
    private final MovieService movieService;

    public ScreeningService(ScreeningRepository screeningRepository, MovieService movieService) {
        this.screeningRepository = screeningRepository;
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
        // shortcut: only the exact same start time is caught, not overlapping runtimes; upgrade if halls need real scheduling
        if (screeningRepository.existsByHallAndStartTime(form.getHall(), form.getStartTime())) {
            throw new IllegalStateException("That hall is already used at this time");
        }
        Screening s = new Screening();
        s.setMovie(movieService.get(form.getMovieId()));
        s.setStartTime(form.getStartTime());
        s.setHall(form.getHall());
        s.setTotalSeats(form.getTotalSeats());
        s.setPrice(form.getPrice());
        return screeningRepository.save(s);
    }
}
