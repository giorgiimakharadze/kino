package com.kinoapp.kino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A single showing of a movie in a hall at a given time
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private String hall;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private BigDecimal price;

    // No cascade: a screening with bookings must not be deleted silently
    @OneToMany(mappedBy = "screening")
    private List<Booking> bookings = new ArrayList<>();
}
