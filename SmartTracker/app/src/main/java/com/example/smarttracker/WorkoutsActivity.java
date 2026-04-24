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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.adapter.WorkoutAdapter;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class WorkoutsActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutActionListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressWorkouts;
    RecyclerView recyclerWorkouts;
    WorkoutAdapter workoutAdapter;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;
    RequestQueue queue;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_workouts);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressWorkouts = (ProgressBar) findViewById(R.id.progressWorkouts);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyWorkouts);
        recyclerWorkouts = (RecyclerView) findViewById(R.id.recyclerWorkouts);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        workoutAdapter = new WorkoutAdapter(this);
        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(this));
        recyclerWorkouts.setAdapter(workoutAdapter);

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

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddWorkoutDialog();
            }
        });

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

        if (getIntent().getBooleanExtra("openAddDialog", false)) {
            showAddWorkoutDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadWorkouts();
        }
    }

    private void loadWorkouts() {
        progressWorkouts.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "getworkouts.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressWorkouts.setVisibility(View.GONE);
                        workoutAdapter.setWorkouts(response);
                        if (response.length() == 0) {
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
                                "Failed to load workouts", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout, null);

        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etWorkoutTitle);
        final TextInputEditText etDuration = (TextInputEditText) dialogView.findViewById(R.id.etDuration);
        final TextInputEditText etCalories = (TextInputEditText) dialogView.findViewById(R.id.etCalories);
        final RadioGroup rgIntensity = (RadioGroup) dialogView.findViewById(R.id.rgIntensity);

        new AlertDialog.Builder(this)
                .setTitle("Log Workout")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(WorkoutsActivity.this,
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String durationText = etDuration.getText().toString().trim();
                        String caloriesText = etCalories.getText().toString().trim();

                        final String duration;
                        if (durationText.isEmpty()) {
                            duration = "0";
                        } else {
                            duration = durationText;
                        }

                        final String calories;
                        if (caloriesText.isEmpty()) {
                            calories = "0";
                        } else {
                            calories = caloriesText;
                        }

                        final String intensity;
                        if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                            intensity = "LOW";
                        } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                            intensity = "HIGH";
                        } else {
                            intensity = "MEDIUM";
                        }

                        String url = BASE_URL + "addworkout.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
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
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                params.put("title", title);
                                params.put("duration_minutes", duration);
                                params.put("calories", calories);
                                params.put("intensity", intensity);
                                return params;
                            }
                        };

                        queue.add(request);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggle(final int workoutId) {
        String url = BASE_URL + "toggleworkout.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadWorkouts();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WorkoutsActivity.this,
                                "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("workout_id", String.valueOf(workoutId));
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDelete(final int workoutId, String title) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = BASE_URL + "deleteworkout.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        loadWorkouts();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(WorkoutsActivity.this,
                                                "Failed to delete", Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("workout_id", String.valueOf(workoutId));
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                return params;
                            }
                        };

                        queue.add(request);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
