package com.smarttracker.repository;

import com.smarttracker.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdAndDate(Long userId, LocalDate date);
    long countByUserIdAndDateAndCompletedTrue(Long userId, LocalDate date);
    long countByUserIdAndDate(Long userId, LocalDate date);
    List<Task> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
