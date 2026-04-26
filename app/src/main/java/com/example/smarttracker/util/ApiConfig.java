package com.example.smarttracker.util;

public class ApiConfig {

    public static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    public static final String LOGIN          = BASE_URL + "login.php";
    public static final String REGISTER       = BASE_URL + "register.php";

    public static final String GET_HABITS     = BASE_URL + "getHabits.php";
    public static final String ADD_HABIT      = BASE_URL + "addHabit.php";
    public static final String DELETE_HABIT   = BASE_URL + "deleteHabit.php";

    public static final String GET_TASKS      = BASE_URL + "getTasks.php";
    public static final String TOGGLE_TASK    = BASE_URL + "toggleTask.php";

    public static final String GET_WORKOUTS   = BASE_URL + "getWorkouts.php";
    public static final String ADD_WORKOUT    = BASE_URL + "addWorkout.php";
    public static final String TOGGLE_WORKOUT = BASE_URL + "toggleWorkout.php";
    public static final String DELETE_WORKOUT = BASE_URL + "deleteWorkout.php";

    public static final String GET_PROGRESS   = BASE_URL + "getProgress.php";
}
