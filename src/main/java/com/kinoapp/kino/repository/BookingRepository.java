package com.kinoapp.kino.repository;

import com.kinoapp.kino.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Custom query: seats already taken for a screening (0 if no bookings)
    @Query("select coalesce(sum(b.seats), 0) from Booking b where b.screening.id = :screeningId")
    int countBookedSeats(@Param("screeningId") Long screeningId);
}
