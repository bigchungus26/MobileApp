package com.example.smarttracker.api;

import com.example.smarttracker.model.AuthResponse;
import com.example.smarttracker.model.Habit;
import com.example.smarttracker.model.HabitRequest;
import com.example.smarttracker.model.LoginRequest;
import com.example.smarttracker.model.ProgressResponse;
import com.example.smarttracker.model.RegisterRequest;
import com.example.smarttracker.model.Task;
import com.example.smarttracker.model.Workout;
import com.example.smarttracker.model.WorkoutRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Auth
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // Habits
    @GET("api/habits")
    Call<List<Habit>> getActiveHabits();

    @POST("api/habits")
    Call<Habit> createHabit(@Body HabitRequest request);

    @PUT("api/habits/{id}")
    Call<Habit> updateHabit(@Path("id") long id, @Body HabitRequest request);

    @DELETE("api/habits/{id}")
    Call<Void> deleteHabit(@Path("id") long id);

    // Tasks
    @GET("api/tasks/today")
    Call<List<Task>> getTodayTasks();

    @PUT("api/tasks/{id}/toggle")
    Call<Task> toggleTask(@Path("id") long id);

    // Workouts
    @GET("api/workouts/today")
    Call<List<Workout>> getTodayWorkouts();

    @GET("api/workouts")
    Call<List<Workout>> getAllWorkouts();

    @POST("api/workouts")
    Call<Workout> createWorkout(@Body WorkoutRequest request);

    @PUT("api/workouts/{id}")
    Call<Workout> updateWorkout(@Path("id") long id, @Body WorkoutRequest request);

    @PUT("api/workouts/{id}/toggle")
    Call<Workout> toggleWorkout(@Path("id") long id);

    @DELETE("api/workouts/{id}")
    Call<Void> deleteWorkout(@Path("id") long id);

    // Progress
    @GET("api/progress")
    Call<ProgressResponse> getProgress();
}
