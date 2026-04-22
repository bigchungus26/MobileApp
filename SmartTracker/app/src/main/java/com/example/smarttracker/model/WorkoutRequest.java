package com.example.smarttracker.model;

public class WorkoutRequest {
    private String title;
    private int durationMinutes;
    private int calories;
    private String intensity;
    private String date;

    public WorkoutRequest(String title, int durationMinutes, int calories, String intensity, String date) {
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.intensity = intensity;
        this.date = date;
    }

    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCalories() { return calories; }
    public String getIntensity() { return intensity; }
    public String getDate() { return date; }
}
