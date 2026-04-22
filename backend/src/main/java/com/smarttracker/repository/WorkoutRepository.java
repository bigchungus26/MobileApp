package com.smarttracker.repository;

import com.smarttracker.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserIdAndDate(Long userId, LocalDate date);
    List<Workout> findByUserId(Long userId);
    long countByUserIdAndDateAndCompletedTrue(Long userId, LocalDate date);
    long countByUserIdAndDate(Long userId, LocalDate date);
    List<Workout> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
