package com.example.smarttracker.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    public String title;

    public String description;

    public String category;

    @NonNull
    public String frequency = "DAILY";

    public int streak = 0;

    public boolean active = true;

    public Habit(int userId, @NonNull String title, String description,
                 String category, @NonNull String frequency) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.frequency = frequency;
    }
}
