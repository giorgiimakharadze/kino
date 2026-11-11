package com.kinoapp.kino.repository;

import com.kinoapp.kino.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    // Custom query: future screenings of one movie, soonest first
    @Query("select s from Screening s where s.movie.id = :movieId and s.startTime > :now order by s.startTime")
    List<Screening> findUpcomingByMovie(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);
}
