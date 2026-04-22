package com.smarttracker.controller;

import com.smarttracker.dto.HabitRequest;
import com.smarttracker.model.Habit;
import com.smarttracker.model.User;
import com.smarttracker.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public ResponseEntity<List<Habit>> getActiveHabits(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(habitService.getActiveHabits(user.getId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Habit>> getAllHabits(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(habitService.getAllHabits(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody HabitRequest request) {
        return ResponseEntity.ok(habitService.createHabit(user, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habit> updateHabit(@AuthenticationPrincipal User user,
                                              @PathVariable Long id,
                                              @Valid @RequestBody HabitRequest request) {
        return ResponseEntity.ok(habitService.updateHabit(id, user.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@AuthenticationPrincipal User user,
                                             @PathVariable Long id) {
        habitService.deleteHabit(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
