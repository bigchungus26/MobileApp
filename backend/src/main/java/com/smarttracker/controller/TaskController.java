package com.smarttracker.controller;

import com.smarttracker.model.Task;
import com.smarttracker.model.User;
import com.smarttracker.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<Task>> getTodayTasks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTodayTasks(user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasksByDate(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskService.getTasksByDate(user.getId(), date));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Task> toggleTask(@AuthenticationPrincipal User user,
                                            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.toggleTaskCompletion(id, user.getId()));
    }
}
