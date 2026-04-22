package com.smarttracker.service;

import com.smarttracker.dto.WorkoutRequest;
import com.smarttracker.model.User;
import com.smarttracker.model.Workout;
import com.smarttracker.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public List<Workout> getTodayWorkouts(Long userId) {
        return workoutRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public List<Workout> getAllWorkouts(Long userId) {
        return workoutRepository.findByUserId(userId);
    }

    public Workout createWorkout(User user, WorkoutRequest request) {
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setTitle(request.getTitle());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCalories(request.getCalories());
        workout.setIntensity(request.getIntensity());
        workout.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        return workoutRepository.save(workout);
    }

    public Workout updateWorkout(Long workoutId, Long userId, WorkoutRequest request) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        workout.setTitle(request.getTitle());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCalories(request.getCalories());
        workout.setIntensity(request.getIntensity());
        if (request.getDate() != null) {
            workout.setDate(request.getDate());
        }
        return workoutRepository.save(workout);
    }

    public Workout toggleWorkoutCompletion(Long workoutId, Long userId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        workout.setCompleted(!workout.isCompleted());
        workout.setCompletedAt(workout.isCompleted() ? LocalDateTime.now() : null);
        return workoutRepository.save(workout);
    }

    public void deleteWorkout(Long workoutId, Long userId) {
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        if (!workout.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        workoutRepository.delete(workout);
    }
}
