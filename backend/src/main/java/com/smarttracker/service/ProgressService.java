package com.smarttracker.service;

import com.smarttracker.dto.ProgressResponse;
import com.smarttracker.repository.TaskRepository;
import com.smarttracker.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProgressService {

    private final TaskRepository taskRepository;
    private final WorkoutRepository workoutRepository;

    public ProgressService(TaskRepository taskRepository, WorkoutRepository workoutRepository) {
        this.taskRepository = taskRepository;
        this.workoutRepository = workoutRepository;
    }

    public ProgressResponse getProgress(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        ProgressResponse response = new ProgressResponse();

        // Today's stats
        response.setHabitsCompleted(taskRepository.countByUserIdAndDateAndCompletedTrue(userId, today));
        response.setHabitsTotal(taskRepository.countByUserIdAndDate(userId, today));
        response.setWorkoutsCompleted(workoutRepository.countByUserIdAndDateAndCompletedTrue(userId, today));
        response.setWorkoutsTotal(workoutRepository.countByUserIdAndDate(userId, today));

        // Weekly progress — calculate daily completion percentages
        Map<String, Double> dailyProgress = new LinkedHashMap<>();
        long weekTotalItems = 0;
        long weekCompletedItems = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            if (day.isAfter(today)) break;

            long dayTasks = taskRepository.countByUserIdAndDate(userId, day);
            long dayTasksDone = taskRepository.countByUserIdAndDateAndCompletedTrue(userId, day);
            long dayWorkouts = workoutRepository.countByUserIdAndDate(userId, day);
            long dayWorkoutsDone = workoutRepository.countByUserIdAndDateAndCompletedTrue(userId, day);

            long total = dayTasks + dayWorkouts;
            long completed = dayTasksDone + dayWorkoutsDone;

            weekTotalItems += total;
            weekCompletedItems += completed;

            double percent = total > 0 ? (completed * 100.0 / total) : 0;
            dailyProgress.put(day.getDayOfWeek().name(), percent);
        }

        double weeklyPercent = weekTotalItems > 0 ? (weekCompletedItems * 100.0 / weekTotalItems) : 0;
        response.setWeeklyProgressPercent(Math.round(weeklyPercent * 10.0) / 10.0);
        response.setDailyProgress(dailyProgress);

        return response;
    }
}
