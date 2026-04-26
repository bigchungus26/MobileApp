package com.example.smarttracker.data;

public class Workout {

    public int id;
    public int userId;
    public String title;
    public int durationMinutes = 0;
    public int calories = 0;
    public String intensity = "MEDIUM";
    public String date;
    public boolean completed = false;

    public Workout() { }

    public Workout(int userId, String title, int durationMinutes,
                   int calories, String intensity, String date) {
        this.userId = userId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.intensity = intensity;
        this.date = date;
    }
}
