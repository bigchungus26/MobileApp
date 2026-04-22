package com.example.smarttracker.model;

import java.util.Map;

public class ProgressResponse {
    private long habitsCompleted;
    private long habitsTotal;
    private long workoutsCompleted;
    private long workoutsTotal;
    private double weeklyProgressPercent;
    private Map<String, Double> dailyProgress;

    public long getHabitsCompleted() { return habitsCompleted; }
    public long getHabitsTotal() { return habitsTotal; }
    public long getWorkoutsCompleted() { return workoutsCompleted; }
    public long getWorkoutsTotal() { return workoutsTotal; }
    public double getWeeklyProgressPercent() { return weeklyProgressPercent; }
    public Map<String, Double> getDailyProgress() { return dailyProgress; }
}
