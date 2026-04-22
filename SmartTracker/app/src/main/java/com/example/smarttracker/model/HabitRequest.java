package com.example.smarttracker.model;

public class HabitRequest {
    private String title;
    private String description;
    private String category;
    private String frequency;

    public HabitRequest(String title, String description, String category, String frequency) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.frequency = frequency;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getFrequency() { return frequency; }
}
