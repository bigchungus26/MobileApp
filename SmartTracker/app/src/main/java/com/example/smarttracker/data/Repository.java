package com.example.smarttracker.data;

import android.content.Context;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Repository {

    private static Repository instance;

    private final AppDatabase db;

    private Repository(Context context) {
        this.db = AppDatabase.get(context);
    }

    public static Repository get(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final Integer userId;
        public final String name;
        public final String email;

        public AuthResult(boolean success, String message, Integer userId, String name, String email) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.name = name;
            this.email = email;
        }

        public static AuthResult fail(String message) {
            return new AuthResult(false, message, null, null, null);
        }

        public static AuthResult ok(User user) {
            return new AuthResult(true, "ok", user.id, user.name, user.email);
        }
    }

    public AuthResult register(String name, String email, String password) {
        if (db.userDao().countByEmail(email) > 0) {
            return AuthResult.fail("Email already registered");
        }
        User user = new User(name, email, hash(password));
        long id = db.userDao().insert(user);
        user.id = (int) id;
        return AuthResult.ok(user);
    }

    public AuthResult login(String email, String password) {
        User user = db.userDao().findByEmail(email);
        if (user == null) {
            return AuthResult.fail("User not found");
        }
        if (!user.passwordHash.equals(hash(password))) {
            return AuthResult.fail("Invalid password");
        }
        return AuthResult.ok(user);
    }

    public List<Habit> getHabits(int userId) {
        return db.habitDao().getActiveForUser(userId);
    }

    public void addHabit(int userId, String title, String description,
                         String category, String frequency) {
        Habit habit = new Habit(userId, title, description, category, frequency);
        long habitId = db.habitDao().insert(habit);
        db.taskDao().insert(new Task(userId, (int) habitId, today()));
    }

    public void deleteHabit(int habitId, int userId) {
        db.habitDao().softDelete(habitId, userId);
    }

    public List<TaskView> getTodayTasks(int userId) {
        return db.taskDao().getForUserAndDate(userId, today());
    }

    public void toggleTask(int taskId, int userId) {
        Integer current = db.taskDao().getCompleted(taskId, userId);
        if (current == null) return;
        db.taskDao().setCompleted(taskId, userId, current == 0);
    }

    public List<Workout> getTodayWorkouts(int userId) {
        return db.workoutDao().getForUserAndDate(userId, today());
    }

    public void addWorkout(int userId, String title, int durationMinutes,
                           int calories, String intensity) {
        db.workoutDao().insert(new Workout(userId, title, durationMinutes,
                calories, intensity, today()));
    }

    public void toggleWorkout(int workoutId, int userId) {
        Integer current = db.workoutDao().getCompleted(workoutId, userId);
        if (current == null) return;
        db.workoutDao().setCompleted(workoutId, userId, current == 0);
    }

    public void deleteWorkout(int workoutId, int userId) {
        db.workoutDao().delete(workoutId, userId);
    }

    public static class ProgressSummary {
        public final int habitsCompleted;
        public final int habitsTotal;
        public final int workoutsCompleted;
        public final int workoutsTotal;
        public final double weeklyPercent;
        public final Map<String, Double> daily;

        ProgressSummary(int hDone, int hTotal, int wDone, int wTotal,
                        double weekly, Map<String, Double> daily) {
            this.habitsCompleted = hDone;
            this.habitsTotal = hTotal;
            this.workoutsCompleted = wDone;
            this.workoutsTotal = wTotal;
            this.weeklyPercent = weekly;
            this.daily = daily;
        }
    }

    public ProgressSummary getProgress(int userId) {
        String today = today();

        int habitsTotal = db.taskDao().countForDate(userId, today);
        int habitsDone = db.taskDao().countCompletedForDate(userId, today);
        int workoutsTotal = db.workoutDao().countForDate(userId, today);
        int workoutsDone = db.workoutDao().countCompletedForDate(userId, today);

        Map<String, Double> daily = new LinkedHashMap<>();
        int weekTotal = 0;
        int weekDone = 0;

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offsetToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -offsetToMonday);

        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.US);

        for (int i = 0; i < 7; i++) {
            String day = dateFmt.format(cal.getTime());

            int dt = db.taskDao().countForDate(userId, day);
            int dd = db.taskDao().countCompletedForDate(userId, day);
            int wt = db.workoutDao().countForDate(userId, day);
            int wd = db.workoutDao().countCompletedForDate(userId, day);

            int total = dt + wt;
            int done = dd + wd;
            weekTotal += total;
            weekDone += done;

            double pct = total > 0 ? Math.round(((double) done / total) * 1000.0) / 10.0 : 0.0;
            daily.put(dayFmt.format(cal.getTime()), pct);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        double weeklyPct = weekTotal > 0
                ? Math.round(((double) weekDone / weekTotal) * 1000.0) / 10.0
                : 0.0;

        return new ProgressSummary(habitsDone, habitsTotal, workoutsDone, workoutsTotal,
                weeklyPct, daily);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
