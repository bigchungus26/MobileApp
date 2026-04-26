package com.example.smarttracker.data;

public class Task {

    public int id;
    public int userId;
    public int habitId;
    public String title;
    public String description;
    public String date;
    public boolean completed = false;

    public Task() { }

    public Task(int userId, int habitId, String date) {
        this.userId = userId;
        this.habitId = habitId;
        this.date = date;
    }
}
