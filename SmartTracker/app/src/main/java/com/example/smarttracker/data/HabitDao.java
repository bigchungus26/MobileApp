package com.example.smarttracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HabitDao {

    @Insert
    long insert(Habit habit);

    @Query("SELECT * FROM habits WHERE user_id = :userId AND active = 1 ORDER BY id DESC")
    List<Habit> getActiveForUser(int userId);

    @Query("UPDATE habits SET active = 0 WHERE id = :habitId AND user_id = :userId")
    void softDelete(int habitId, int userId);
}
