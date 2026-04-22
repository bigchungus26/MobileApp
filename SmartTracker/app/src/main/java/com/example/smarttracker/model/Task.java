package com.example.smarttracker.model;

public class Task {
    private long id;
    private Habit habit;
    private String date;
    private boolean completed;
    private String completedAt;

    public long getId() { return id; }
    public Habit getHabit() { return habit; }
    public String getDate() { return date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getCompletedAt() { return completedAt; }
}
