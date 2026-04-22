package com.smarttracker.dto;

import com.smarttracker.model.Habit;
import jakarta.validation.constraints.NotBlank;

public class HabitRequest {

    @NotBlank
    private String title;

    private String description;

    private String category;

    private Habit.Frequency frequency = Habit.Frequency.DAILY;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Habit.Frequency getFrequency() { return frequency; }
    public void setFrequency(Habit.Frequency frequency) { this.frequency = frequency; }
}
