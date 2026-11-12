package com.kinoapp.kino.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

// Loads real films from the free API (no API key needed)
@Service
public class MovieApiClient {

    private static final String FILMS_URL = "https://ghibliapi.vercel.app/films";

    private final RestClient restClient;

    public MovieApiClient() {
        // Short timeouts so a slow API cannot block app startup
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    // Only the fields we use
    public record Film(String title, String description,
                       @JsonProperty("release_date") String releaseDate,
                       @JsonProperty("running_time") String runningTime,
                       String image) {
    }

    public List<Film> fetchFilms() {
        Film[] films = restClient.get().uri(FILMS_URL).retrieve().body(Film[].class);
        return films == null ? List.of() : List.of(films);
    }
}
