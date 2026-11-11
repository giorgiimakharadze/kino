package com.kinoapp.kino.repository;

import com.kinoapp.kino.entity.Screening;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    // future screenings of one movie, soonest first
    @Query("select s from Screening s where s.movie.id = :movieId and s.startTime > :now order by s.startTime")
    List<Screening> findUpcomingByMovie(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);

    // is this hall already booked for that exact start time?
    boolean existsByHallAndStartTime(String hall, LocalDateTime startTime);

    // Same as findById but locks the row until the transaction ends
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Screening s where s.id = :id")
    Optional<Screening> findByIdForUpdate(@Param("id") Long id);
}
