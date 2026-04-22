package com.example.smarttracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert
    long insert(Workout workout);

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND date = :date ORDER BY id DESC")
    List<Workout> getForUserAndDate(int userId, String date);

    @Query("SELECT completed FROM workouts WHERE id = :workoutId AND user_id = :userId LIMIT 1")
    Integer getCompleted(int workoutId, int userId);

    @Query("UPDATE workouts SET completed = :completed WHERE id = :workoutId AND user_id = :userId")
    void setCompleted(int workoutId, int userId, boolean completed);

    @Query("DELETE FROM workouts WHERE id = :workoutId AND user_id = :userId")
    void delete(int workoutId, int userId);

    @Query("SELECT COUNT(*) FROM workouts WHERE user_id = :userId AND date = :date")
    int countForDate(int userId, String date);

    @Query("SELECT COUNT(*) FROM workouts WHERE user_id = :userId AND date = :date AND completed = 1")
    int countCompletedForDate(int userId, String date);
}
