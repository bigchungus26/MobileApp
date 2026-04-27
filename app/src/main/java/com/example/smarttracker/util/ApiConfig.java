package com.example.smarttracker.util;

//central place that lists every backend endpoint the app talks to
public class ApiConfig {

    //10.0.2.2 is the Android emulator's alias for the laptop running XAMPP
    public static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    //auth endpoints
    public static final String LOGIN          = BASE_URL + "login.php";
    public static final String REGISTER       = BASE_URL + "register.php";

    //habit endpoints
    public static final String GET_HABITS     = BASE_URL + "getHabits.php";
    public static final String ADD_HABIT      = BASE_URL + "addHabit.php";
    public static final String DELETE_HABIT   = BASE_URL + "deleteHabit.php";

    //task endpoints used by the today list on the home screen
    public static final String GET_TASKS      = BASE_URL + "getTasks.php";
    public static final String TOGGLE_TASK    = BASE_URL + "toggleTask.php";

    //workout endpoints
    public static final String GET_WORKOUTS   = BASE_URL + "getWorkouts.php";
    public static final String ADD_WORKOUT    = BASE_URL + "addWorkout.php";
    public static final String TOGGLE_WORKOUT = BASE_URL + "toggleWorkout.php";
    public static final String DELETE_WORKOUT = BASE_URL + "deleteWorkout.php";

    //aggregate stats used by the home dashboard and progress screen
    public static final String GET_PROGRESS   = BASE_URL + "getProgress.php";
}
