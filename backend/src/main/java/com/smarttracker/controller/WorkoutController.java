package com.smarttracker.controller;

import com.smarttracker.dto.WorkoutRequest;
import com.smarttracker.model.User;
import com.smarttracker.model.Workout;
import com.smarttracker.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<Workout>> getTodayWorkouts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutService.getTodayWorkouts(user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getAllWorkouts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutService.getAllWorkouts(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.createWorkout(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.updateWorkout(id, user.getId(), request));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Workout> toggleWorkout(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(workoutService.toggleWorkoutCompletion(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@AuthenticationPrincipal User user,
                                               @PathVariable Long id) {
        workoutService.deleteWorkout(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
