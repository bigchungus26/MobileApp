package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class WorkoutsActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutActionListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private static final String PREF_NAME = "smarttracker";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    BottomNavigationView bottomNav;
    FloatingActionButton fabAdd;
    ImageView ivProfile;
    TextView tvUserName, tvEmpty;
    ProgressBar progressWorkouts;
    RecyclerView recyclerWorkouts;
    WorkoutAdapter workoutAdapter;
    RequestQueue queue;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (prefs.getInt(KEY_USER_ID, -1) == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_workouts);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        recyclerWorkouts = (RecyclerView) findViewById(R.id.recyclerWorkouts);
        progressWorkouts = (ProgressBar) findViewById(R.id.progressWorkouts);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyWorkouts);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        tvUserName.setText(prefs.getString(KEY_USER_NAME, "User"));

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
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(WorkoutsActivity.this, HabitsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(WorkoutsActivity.this, ProgressActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
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
                        .setMessage("Logged in as " + prefs.getString(KEY_USER_EMAIL, ""))
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().apply();
                                startActivity(new Intent(WorkoutsActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        if (getIntent().getBooleanExtra("openAddDialog", false)) {
            showAddWorkoutDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) return;
        loadWorkouts();
    }

    private void loadWorkouts() {
        progressWorkouts.setVisibility(View.VISIBLE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        String url = BASE_URL + "getworkouts.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressWorkouts.setVisibility(View.GONE);
                        workoutAdapter.setWorkouts(response);
                        tvEmpty.setVisibility(response.length() == 0 ? View.VISIBLE : View.GONE);
                        recyclerWorkouts.setVisibility(response.length() == 0 ? View.GONE : View.VISIBLE);
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

                        final String duration = etDuration.getText().toString().trim();
                        final String calories = etCalories.getText().toString().trim();

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
                                params.put("user_id", String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
                                params.put("title", title);
                                params.put("duration_minutes", duration.isEmpty() ? "0" : duration);
                                params.put("calories", calories.isEmpty() ? "0" : calories);
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
    public void onToggle(int workoutId) {
        final int wId = workoutId;
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
                params.put("workout_id", String.valueOf(wId));
                params.put("user_id", String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDelete(int workoutId, String title) {
        final int wId = workoutId;
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
                                params.put("workout_id", String.valueOf(wId));
                                params.put("user_id", String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
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
