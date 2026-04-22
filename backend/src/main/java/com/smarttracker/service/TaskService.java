package com.smarttracker.service;

import com.smarttracker.model.Task;
import com.smarttracker.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTodayTasks(Long userId) {
        return taskRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public List<Task> getTasksByDate(Long userId, LocalDate date) {
        return taskRepository.findByUserIdAndDate(userId, date);
    }

    public Task toggleTaskCompletion(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        task.setCompleted(!task.isCompleted());
        task.setCompletedAt(task.isCompleted() ? LocalDateTime.now() : null);
        return taskRepository.save(task);
    }
}
