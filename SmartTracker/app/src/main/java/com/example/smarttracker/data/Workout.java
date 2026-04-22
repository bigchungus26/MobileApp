package com.example.smarttracker.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workouts")
public class Workout {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    public String title;

    @ColumnInfo(name = "duration_minutes")
    public int durationMinutes = 0;

    public int calories = 0;

    @NonNull
    public String intensity = "MEDIUM";

    @NonNull
    public String date;

    public boolean completed = false;

    public Workout(int userId, @NonNull String title, int durationMinutes,
                   int calories, @NonNull String intensity, @NonNull String date) {
        this.userId = userId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.intensity = intensity;
        this.date = date;
    }
}
