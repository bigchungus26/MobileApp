package com.example.smarttracker.model;

public class Habit {
    private long id;
    private String title;
    private String description;
    private String category;
    private String frequency;
    private int streak;
    private boolean active;
    private String createdAt;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFrequency() { return frequency; }
    public int getStreak() { return streak; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
}
