package com.smarttracker.dto;

import java.util.Map;

public class ProgressResponse {

    private long habitsCompleted;
    private long habitsTotal;
    private long workoutsCompleted;
    private long workoutsTotal;
    private double weeklyProgressPercent;
    private Map<String, Double> dailyProgress;

    public long getHabitsCompleted() { return habitsCompleted; }
    public void setHabitsCompleted(long habitsCompleted) { this.habitsCompleted = habitsCompleted; }

    public long getHabitsTotal() { return habitsTotal; }
    public void setHabitsTotal(long habitsTotal) { this.habitsTotal = habitsTotal; }

    public long getWorkoutsCompleted() { return workoutsCompleted; }
    public void setWorkoutsCompleted(long workoutsCompleted) { this.workoutsCompleted = workoutsCompleted; }

    public long getWorkoutsTotal() { return workoutsTotal; }
    public void setWorkoutsTotal(long workoutsTotal) { this.workoutsTotal = workoutsTotal; }

    public double getWeeklyProgressPercent() { return weeklyProgressPercent; }
    public void setWeeklyProgressPercent(double weeklyProgressPercent) { this.weeklyProgressPercent = weeklyProgressPercent; }

    public Map<String, Double> getDailyProgress() { return dailyProgress; }
    public void setDailyProgress(Map<String, Double> dailyProgress) { this.dailyProgress = dailyProgress; }
}
