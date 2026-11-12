package com.kinoapp.kino.config;

import com.kinoapp.kino.entity.Genre;
import com.kinoapp.kino.entity.Movie;
import com.kinoapp.kino.entity.Screening;
import com.kinoapp.kino.repository.GenreRepository;
import com.kinoapp.kino.repository.MovieRepository;
import com.kinoapp.kino.repository.ScreeningRepository;
import com.kinoapp.kino.service.MovieApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fills the empty in-memory database with movies on every startup
// real films from the Ghibli API, or built-in samples if the API is unreachable
@Slf4j
@Configuration
public class DataSeeder {

    private record Sample(String title, String description, int minutes, LocalDate released,
                          String posterUrl, List<String> genres) {

        static Sample of(String title, String description, int minutes, LocalDate released, String... genres) {
            return new Sample(title, description, minutes, released, null, List.of(genres));
        }
    }

    // The API has no genres, so we add them by title
    private static final Map<String, List<String>> GHIBLI_GENRES = Map.ofEntries(
            Map.entry("Castle in the Sky", List.of("Adventure", "Fantasy")),
            Map.entry("Grave of the Fireflies", List.of("Drama", "War")),
            Map.entry("My Neighbor Totoro", List.of("Family", "Fantasy")),
            Map.entry("Kiki's Delivery Service", List.of("Family", "Fantasy")),
            Map.entry("Only Yesterday", List.of("Drama", "Romance")),
            Map.entry("Porco Rosso", List.of("Adventure", "Comedy")),
            Map.entry("Pom Poko", List.of("Comedy", "Fantasy")),
            Map.entry("Whisper of the Heart", List.of("Drama", "Romance")),
            Map.entry("Princess Mononoke", List.of("Action", "Fantasy")),
            Map.entry("My Neighbors the Yamadas", List.of("Comedy", "Family")),
            Map.entry("Spirited Away", List.of("Fantasy", "Adventure")),
            Map.entry("The Cat Returns", List.of("Fantasy", "Comedy")),
            Map.entry("Howl's Moving Castle", List.of("Fantasy", "Romance")),
            Map.entry("Tales from Earthsea", List.of("Fantasy", "Adventure")),
            Map.entry("Ponyo", List.of("Family", "Fantasy")),
            Map.entry("Arrietty", List.of("Family", "Fantasy")),
            Map.entry("From Up on Poppy Hill", List.of("Drama", "Romance")),
            Map.entry("The Wind Rises", List.of("Drama", "Romance")),
            Map.entry("The Tale of the Princess Kaguya", List.of("Drama", "Fantasy")),
            Map.entry("When Marnie Was There", List.of("Drama", "Mystery")),
            Map.entry("The Red Turtle", List.of("Drama", "Fantasy")),
            Map.entry("Earwig and the Witch", List.of("Family", "Fantasy")));

    // Used only when the API cannot be reached
    private static final List<Sample> FALLBACK = List.of(
            Sample.of("Edge of Tomorrow Ridge", "A soldier relives the same battle until he can win it.", 113, LocalDate.of(2022, 6, 3), "Action", "Sci-Fi"),
            Sample.of("The Laughing Lighthouse", "Three friends run a haunted lighthouse as a bed and breakfast.", 98, LocalDate.of(2023, 2, 14), "Comedy"),
            Sample.of("Quiet Rivers", "A family secret surfaces during a summer by the river.", 126, LocalDate.of(2021, 9, 10), "Drama"),
            Sample.of("Midnight Corridor", "A night guard discovers a door that was not there yesterday.", 101, LocalDate.of(2024, 10, 31), "Horror", "Thriller"),
            Sample.of("Orbit Seven", "A crew races to repair their ship before the oxygen runs out.", 134, LocalDate.of(2024, 5, 17), "Sci-Fi", "Thriller"),
            Sample.of("Paper Dragons", "A shy girl brings her origami creatures to life.", 92, LocalDate.of(2023, 12, 8), "Animation", "Comedy"),
            Sample.of("Last Train to Lisbon", "Two strangers meet on an overnight train and change each other's plans.", 108, LocalDate.of(2022, 4, 22), "Romance", "Drama"),
            Sample.of("Iron Harbor", "A retired captain takes one last job to save his port.", 122, LocalDate.of(2025, 1, 24), "Action", "Drama"),
            Sample.of("The Unreliable Witness", "A lawyer must trust the one person who always lies.", 115, LocalDate.of(2021, 3, 5), "Thriller"),
            Sample.of("Love and Lasagna", "A food critic falls for the chef she just gave a bad review.", 96, LocalDate.of(2025, 2, 14), "Romance", "Comedy"));

    // Tests switch this off (app.seed=false) so they do not call the web API
    @Bean
    @ConditionalOnProperty(name = "app.seed", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seed(GenreRepository genreRepository, MovieRepository movieRepository,
                           ScreeningRepository screeningRepository, MovieApiClient apiClient) {
        return args -> {
            // Never add the data twice
            if (genreRepository.count() > 0) {
                return;
            }

            List<Sample> samples = loadSamples(apiClient);
            Map<String, Genre> genres = new HashMap<>();

            for (int i = 0; i < samples.size(); i++) {
                Sample s = samples.get(i);
                Movie movie = new Movie();
                movie.setTitle(s.title());
                movie.setDescription(s.description());
                movie.setDurationMinutes(s.minutes());
                movie.setReleaseDate(s.released());
                // For API films this is the poster's web address, shown straight from there
                movie.setPosterPath(s.posterUrl());
                for (String name : s.genres()) {
                    movie.getGenres().add(genres.computeIfAbsent(name, n -> genreRepository.save(new Genre(n))));
                }
                movieRepository.save(movie);

                // Two future screenings per movie; hall/time differ per movie so none clash
                LocalDate firstDay = LocalDate.now().plusDays(1 + i / 5);
                int hour = 12 + 2 * (i % 5);
                BigDecimal price = BigDecimal.valueOf(8 + (i % 3) * 1.5).setScale(2);
                int seats = 40 + 10 * (i % 4);
                screeningRepository.save(screening(movie, "Hall 1", firstDay.atTime(hour, 0), seats, price));
                screeningRepository.save(screening(movie, "Hall 2", firstDay.plusDays(2).atTime(hour, 0), seats, price));
            }
        };
    }

    private List<Sample> loadSamples(MovieApiClient apiClient) {
        try {
            List<Sample> fromApi = apiClient.fetchFilms().stream()
                    .map(f -> new Sample(f.title(), f.description(), Integer.parseInt(f.runningTime()),
                            LocalDate.of(Integer.parseInt(f.releaseDate()), 1, 1), f.image(),
                            GHIBLI_GENRES.getOrDefault(f.title(), List.of("Animation"))))
                    .toList();
            if (!fromApi.isEmpty()) {
                return fromApi;
            }
        } catch (RuntimeException e) {
            log.warn("Could not load movies from the API, using built-in samples: {}", e.toString());
        }
        return FALLBACK;
    }

    private Screening screening(Movie movie, String hall, LocalDateTime start, int seats, BigDecimal price) {
        Screening s = new Screening();
        s.setMovie(movie);
        s.setHall(hall);
        s.setStartTime(start);
        s.setTotalSeats(seats);
        s.setPrice(price);
        return s;
    }
}
