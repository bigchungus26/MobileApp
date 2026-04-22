package com.smarttracker.dto;

import com.smarttracker.model.Workout;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class WorkoutRequest {

    @NotBlank
    private String title;

    private int durationMinutes;

    private int calories;

    private Workout.Intensity intensity = Workout.Intensity.MEDIUM;

    private LocalDate date;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public Workout.Intensity getIntensity() { return intensity; }
    public void setIntensity(Workout.Intensity intensity) { this.intensity = intensity; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
