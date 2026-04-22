package com.example.smarttracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(Task task);

    @Query("SELECT t.id AS id, t.completed AS completed, h.title AS title, h.description AS description " +
            "FROM tasks t JOIN habits h ON t.habit_id = h.id " +
            "WHERE t.user_id = :userId AND t.date = :date AND h.active = 1")
    List<TaskView> getForUserAndDate(int userId, String date);

    @Query("SELECT completed FROM tasks WHERE id = :taskId AND user_id = :userId LIMIT 1")
    Integer getCompleted(int taskId, int userId);

    @Query("SELECT completed FROM tasks WHERE user_id = :userId AND habit_id = :habitId AND date = :date LIMIT 1")
    Integer getCompletedForHabitOnDate(int userId, int habitId, String date);

    @Query("SELECT habit_id FROM tasks WHERE id = :taskId LIMIT 1")
    Integer getHabitIdForTask(int taskId);

    @Query("UPDATE tasks SET completed = :completed WHERE id = :taskId AND user_id = :userId")
    void setCompleted(int taskId, int userId, boolean completed);

    @Query("SELECT COUNT(*) FROM tasks t JOIN habits h ON t.habit_id = h.id " +
            "WHERE t.user_id = :userId AND t.date = :date AND h.active = 1")
    int countForDate(int userId, String date);

    @Query("SELECT COUNT(*) FROM tasks t JOIN habits h ON t.habit_id = h.id " +
            "WHERE t.user_id = :userId AND t.date = :date AND t.completed = 1 AND h.active = 1")
    int countCompletedForDate(int userId, String date);
}
