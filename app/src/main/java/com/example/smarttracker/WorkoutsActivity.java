package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.adapter.WorkoutAdapter;
import com.example.smarttracker.data.Workout;
import com.example.smarttracker.util.ApiConfig;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutsActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutActionListener {

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressWorkouts;
    RecyclerView recyclerWorkouts;
    WorkoutAdapter workoutAdapter;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;
    SessionManager sessionManager;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //gate this screen behind a valid session
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(WorkoutsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_workouts);

        queue = Volley.newRequestQueue(WorkoutsActivity.this);

        //link the layout views
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressWorkouts = (ProgressBar) findViewById(R.id.progressWorkouts);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyWorkouts);
        recyclerWorkouts = (RecyclerView) findViewById(R.id.recyclerWorkouts);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        //set up the workouts list
        workoutAdapter = new WorkoutAdapter(WorkoutsActivity.this);
        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(WorkoutsActivity.this));
        recyclerWorkouts.setAdapter(workoutAdapter);

        //highlight the workouts tab in the bottom nav
        bottomNav.setSelectedItemId(R.id.nav_workouts);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_workouts) {
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(WorkoutsActivity.this, MainActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(WorkoutsActivity.this, HabitsActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(WorkoutsActivity.this, ProgressActivity.class));
                    finish();
                    return false;
                }
                return false;
            }
        });

        //add button opens the log workout dialog
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAddWorkoutDialog(); }
        });

        //profile icon opens the account dialog with log out
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(WorkoutsActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + sessionManager.getUserEmail())
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sessionManager.logout();
                                startActivity(new Intent(WorkoutsActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        loadWorkouts();

        //open the dialog right away if we got here from the add chooser
        if (getIntent().getBooleanExtra("openAddDialog", false)) {
            showAddWorkoutDialog();
        }
    }

    //refresh the list whenever the user comes back to this screen
    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadWorkouts();
        }
    }

    //fetch the user's workouts from the backend
    private void loadWorkouts() {
        progressWorkouts.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_WORKOUTS + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressWorkouts.setVisibility(View.GONE);
                        //convert the JSON array into Workout objects
                        List<Workout> workouts = new ArrayList<>();
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                Workout w = new Workout();
                                w.id = o.getInt("id");
                                w.userId = o.optInt("user_id");
                                w.title = o.optString("title");
                                w.durationMinutes = o.optInt("duration_minutes", 0);
                                w.calories = o.optInt("calories", 0);
                                w.intensity = o.optString("intensity", "MEDIUM");
                                w.date = o.optString("date");
                                w.completed = o.optInt("completed", 0) == 1;
                                workouts.add(w);
                            }
                        } catch (Exception ignored) { }

                        workoutAdapter.setWorkouts(workouts);
                        //show the empty state when there are no workouts logged
                        if (workouts.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerWorkouts.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerWorkouts.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressWorkouts.setVisibility(View.GONE);
                        Toast.makeText(WorkoutsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    //popup form to log a new workout
    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(WorkoutsActivity.this)
                .inflate(R.layout.dialog_add_workout, null);

        //grab the form inputs from the dialog view
        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etWorkoutTitle);
        final TextInputEditText etDuration = (TextInputEditText) dialogView.findViewById(R.id.etDuration);
        final TextInputEditText etCalories = (TextInputEditText) dialogView.findViewById(R.id.etCalories);
        final RadioGroup rgIntensity = (RadioGroup) dialogView.findViewById(R.id.rgIntensity);

        new AlertDialog.Builder(WorkoutsActivity.this)
                .setTitle("Log Workout")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //title is required
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(WorkoutsActivity.this,
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //parse the number fields, default to 0 if blank
                        int duration = parseIntOrZero(etDuration.getText().toString().trim());
                        int calories = parseIntOrZero(etCalories.getText().toString().trim());

                        //read the selected intensity radio button
                        String intensity;
                        if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                            intensity = "LOW";
                        } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                            intensity = "HIGH";
                        } else {
                            intensity = "MEDIUM";
                        }

                        addWorkout(title, duration, calories, intensity);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //POST the new workout to the backend
    private void addWorkout(final String title, final int duration,
                            final int calories, final String intensity) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.ADD_WORKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(WorkoutsActivity.this,
                                "Workout logged!", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WorkoutsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("title", title);
                params.put("duration_minutes", String.valueOf(duration));
                params.put("calories", String.valueOf(calories));
                params.put("intensity", intensity);
                return params;
            }
        };

        queue.add(request);
    }

    //small helper that returns 0 when the text is empty or not a number
    private int parseIntOrZero(String s) {
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    //called when the user checks the workout's done checkbox
    @Override
    public void onToggle(final int workoutId) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.TOGGLE_WORKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { loadWorkouts(); }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("workout_id", String.valueOf(workoutId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        queue.add(request);
    }

    //triggered by the trash icon, asks the user to confirm first
    @Override
    public void onDelete(final int workoutId, String title) {
        new AlertDialog.Builder(WorkoutsActivity.this)
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteWorkout(workoutId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //call the delete endpoint then refresh the list
    private void deleteWorkout(final int workoutId) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.DELETE_WORKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { loadWorkouts(); }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WorkoutsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("workout_id", String.valueOf(workoutId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        queue.add(request);
    }
}
