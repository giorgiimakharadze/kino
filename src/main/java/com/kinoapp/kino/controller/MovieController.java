package com.kinoapp.kino.controller;

import com.kinoapp.kino.dto.MovieForm;
import com.kinoapp.kino.entity.Genre;
import com.kinoapp.kino.entity.Movie;
import com.kinoapp.kino.service.MovieService;
import com.kinoapp.kino.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
public class MovieController {

    private static final int PAGE_SIZE = 6;
    // Only these fields may be used for sorting (blocks ?sort=anything)
    private static final Set<String> SORT_FIELDS = Set.of("title", "releaseDate", "durationMinutes");

    private final MovieService movieService;
    private final ScreeningService screeningService;

    public MovieController(MovieService movieService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/movies";
    }

    // Movie list: search, genre filter, sorting and pagination
    @GetMapping("/movies")
    public String list(@RequestParam(defaultValue = "") String title,
                       @RequestParam(defaultValue = "") String genre,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "title") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        String sortField = SORT_FIELDS.contains(sort) ? sort : "title";
        Sort.Direction direction = "desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(direction, sortField));

        Page<Movie> movies = movieService.search(title, genre, pageable);
        model.addAttribute("movies", movies);
        model.addAttribute("genres", movieService.allGenres());
        // Echo the filters back so the form and page links keep them
        model.addAttribute("title", title);
        model.addAttribute("genre", genre);
        model.addAttribute("sort", sortField);
        model.addAttribute("dir", direction == Sort.Direction.DESC ? "desc" : "asc");
        return "movies/list";
    }

    @GetMapping("/movies/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.get(id));
        model.addAttribute("screenings", screeningService.upcomingForMovie(id));
        return "movies/detail";
    }

    @GetMapping("/movies/new")
    public String newForm(Model model) {
        model.addAttribute("movieForm", new MovieForm());
        return formView(model, null, null);
    }

    // Multipart form. fields and poster file bound into MovieForm
    @PostMapping("/movies")
    public String create(@Valid @ModelAttribute("movieForm") MovieForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model, null, null);
        }
        Movie movie = movieService.create(form);
        redirect.addFlashAttribute("message", "Movie added");
        return "redirect:/movies/" + movie.getId();
    }

    @GetMapping("/movies/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Movie movie = movieService.get(id);
        MovieForm form = new MovieForm();
        form.setTitle(movie.getTitle());
        form.setDescription(movie.getDescription());
        form.setDurationMinutes(movie.getDurationMinutes());
        form.setReleaseDate(movie.getReleaseDate());
        movie.getGenres().forEach((Genre g) -> form.getGenreIds().add(g.getId()));
        model.addAttribute("movieForm", form);
        return formView(model, id, movie.getPosterPath());
    }

    // The edit form posts to /movies/{id}?_method=put
    @PutMapping("/movies/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("movieForm") MovieForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formView(model, id, movieService.get(id).getPosterPath());
        }
        movieService.update(id, form);
        redirect.addFlashAttribute("message", "Movie updated");
        return "redirect:/movies/" + id;
    }

    // The delete button posts to /movies/{id}?_method=delete
    @DeleteMapping("/movies/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        movieService.delete(id);
        redirect.addFlashAttribute("message", "Movie deleted");
        return "redirect:/movies";
    }

    // Shared bits for the create/edit page (movieId == null means "new movie")
    private String formView(Model model, Long movieId, String posterPath) {
        model.addAttribute("genres", movieService.allGenres());
        model.addAttribute("movieId", movieId);
        model.addAttribute("posterPath", posterPath);
        return "movies/form";
    }
}
