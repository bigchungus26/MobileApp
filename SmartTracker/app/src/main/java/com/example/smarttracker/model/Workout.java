package com.example.smarttracker.model;

public class Workout {
    private long id;
    private String title;
    private int durationMinutes;
    private int calories;
    private String intensity;
    private String date;
    private boolean completed;
    private String completedAt;
    private String createdAt;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCalories() { return calories; }
    public String getIntensity() { return intensity; }
    public String getDate() { return date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getCompletedAt() { return completedAt; }
    public String getCreatedAt() { return createdAt; }
}
