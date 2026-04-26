package com.example.smarttracker.data;

public class Habit {

    public int id;
    public int userId;
    public String title;
    public String description;
    public String category;
    public String frequency = "DAILY";
    public int streak = 0;
    public boolean active = true;

    public Habit() { }

    public Habit(int userId, String title, String description,
                 String category, String frequency) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.frequency = frequency;
    }
}
