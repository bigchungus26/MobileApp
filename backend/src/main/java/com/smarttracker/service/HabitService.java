package com.smarttracker.service;

import com.smarttracker.dto.HabitRequest;
import com.smarttracker.model.Habit;
import com.smarttracker.model.Task;
import com.smarttracker.model.User;
import com.smarttracker.repository.HabitRepository;
import com.smarttracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final TaskRepository taskRepository;

    public HabitService(HabitRepository habitRepository, TaskRepository taskRepository) {
        this.habitRepository = habitRepository;
        this.taskRepository = taskRepository;
    }

    public List<Habit> getActiveHabits(Long userId) {
        return habitRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<Habit> getAllHabits(Long userId) {
        return habitRepository.findByUserId(userId);
    }

    @Transactional
    public Habit createHabit(User user, HabitRequest request) {
        Habit habit = new Habit();
        habit.setUser(user);
        habit.setTitle(request.getTitle());
        habit.setDescription(request.getDescription());
        habit.setCategory(request.getCategory());
        habit.setFrequency(request.getFrequency());
        habit = habitRepository.save(habit);

        // Auto-create today's task for this habit
        Task task = new Task();
        task.setUser(user);
        task.setHabit(habit);
        task.setDate(LocalDate.now());
        taskRepository.save(task);

        return habit;
    }

    public Habit updateHabit(Long habitId, Long userId, HabitRequest request) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        habit.setTitle(request.getTitle());
        habit.setDescription(request.getDescription());
        habit.setCategory(request.getCategory());
        habit.setFrequency(request.getFrequency());
        return habitRepository.save(habit);
    }

    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        habit.setActive(false);
        habitRepository.save(habit);
    }
}
