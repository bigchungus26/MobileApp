package com.example.smarttracker.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "habit_id")
    public int habitId;

    @NonNull
    public String date;

    public boolean completed = false;

    public Task(int userId, int habitId, @NonNull String date) {
        this.userId = userId;
        this.habitId = habitId;
        this.date = date;
    }
}
