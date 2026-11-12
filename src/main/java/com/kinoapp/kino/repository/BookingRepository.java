package com.kinoapp.kino.repository;

import com.kinoapp.kino.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // seats already taken for a screening (0 if no bookings)
    @Query("select coalesce(sum(b.seats), 0) from Booking b where b.screening.id = :screeningId")
    int countBookedSeats(@Param("screeningId") Long screeningId);

    // Used to block deleting a movie that has bookings
    boolean existsByScreening_Movie_Id(Long movieId);

    // Look up a customer's bookings by email
    List<Booking> findByCustomerEmailIgnoreCase(String email);
}
