package com.kinoapp.kino.controller;

import com.kinoapp.kino.dto.ScreeningForm;
import com.kinoapp.kino.service.MovieService;
import com.kinoapp.kino.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ScreeningController {

    private final ScreeningService screeningService;
    private final MovieService movieService;

    public ScreeningController(ScreeningService screeningService, MovieService movieService) {
        this.screeningService = screeningService;
        this.movieService = movieService;
    }

    @GetMapping("/movies/{movieId}/screenings/new")
    public String newForm(@PathVariable Long movieId, Model model) {
        ScreeningForm form = new ScreeningForm();
        form.setMovieId(movieId);
        model.addAttribute("screeningForm", form);
        model.addAttribute("movie", movieService.get(movieId));
        return "screenings/form";
    }

    @PostMapping("/screenings")
    public String create(@Valid @ModelAttribute("screeningForm") ScreeningForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        // Without a movie there is nothing to show on the form (only a hand-made request does this)
        if (result.hasFieldErrors("movieId")) {
            return "redirect:/movies";
        }
        if (!result.hasErrors()) {
            try {
                screeningService.create(form);
                redirect.addFlashAttribute("message", "Screening added");
                return "redirect:/movies/" + form.getMovieId();
            } catch (IllegalStateException e) {
                // e.g. hall already used at that time: show it on the form
                result.reject("screening.failed", e.getMessage());
            }
        }
        model.addAttribute("movie", movieService.get(form.getMovieId()));
        return "screenings/form";
    }
}
