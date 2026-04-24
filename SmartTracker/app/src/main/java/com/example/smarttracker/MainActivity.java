package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskToggleListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    TextView tvUserName, tvHabitsCount, tvWorkoutCount, tvProgressText, tvSeeAll;
    ImageView ivProfile;
    Button btnStartNow;
    ProgressBar progressWeekly;
    RecyclerView recyclerTodayTasks;
    TaskAdapter taskAdapter;
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

        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvHabitsCount = (TextView) findViewById(R.id.tvHabitsCount);
        tvWorkoutCount = (TextView) findViewById(R.id.tvWorkoutCount);
        tvProgressText = (TextView) findViewById(R.id.tvProgressText);
        tvSeeAll = (TextView) findViewById(R.id.tvSeeAll);
        btnStartNow = (Button) findViewById(R.id.btnStartNow);
        progressWeekly = (ProgressBar) findViewById(R.id.progressWeekly);
        recyclerTodayTasks = (RecyclerView) findViewById(R.id.recyclerTodayTasks);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        taskAdapter = new TaskAdapter(this);
        recyclerTodayTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodayTasks.setAdapter(taskAdapter);

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(MainActivity.this, HabitsActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(MainActivity.this, WorkoutsActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(MainActivity.this, ProgressActivity.class));
                    finish();
                    return false;
                }
                return false;
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddChooser();
            }
        });

        btnStartNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddChooser();
            }
        });

        tvSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HabitsActivity.class));
                finish();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + sessionManager.getUserEmail())
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sessionManager.logout();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadData();
        }
    }

    private void loadData() {
        loadTasks();
        loadProgress();
    }

    private void loadTasks() {
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "gettasks.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        taskAdapter.setTasks(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,
                                "Failed to load tasks", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    private void loadProgress() {
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "getprogress.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            long hDone = json.getLong("habitsCompleted");
                            long hTotal = json.getLong("habitsTotal");
                            long wDone = json.getLong("workoutsCompleted");
                            long wTotal = json.getLong("workoutsTotal");
                            double pct = json.getDouble("weeklyProgressPercent");

                            tvHabitsCount.setText(hDone + " / " + hTotal);
                            tvWorkoutCount.setText(wDone + " / " + wTotal);
                            progressWeekly.setProgress((int) pct);

                            if (pct >= 80) {
                                tvProgressText.setText("Amazing! You're crushing it this week!");
                            } else if (pct >= 50) {
                                tvProgressText.setText("You're doing great. Keep your streak going.");
                            } else if (pct > 0) {
                                tvProgressText.setText("Good start! Keep pushing to hit your goals.");
                            } else {
                                tvProgressText.setText("Start completing tasks to see your progress.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tvHabitsCount.setText("0 / 0");
                        tvWorkoutCount.setText("0 / 0");
                    }
                });

        queue.add(request);
    }

    @Override
    public void onToggle(final int taskId) {
        String url = BASE_URL + "toggletask.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,
                                "Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("task_id", String.valueOf(taskId));
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };

        queue.add(request);
    }

    private void showAddChooser() {
        new AlertDialog.Builder(this)
                .setTitle("Add New")
                .setItems(new String[]{"New Habit", "New Workout"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        if (which == 0) {
                            intent = new Intent(MainActivity.this, HabitsActivity.class);
                        } else {
                            intent = new Intent(MainActivity.this, WorkoutsActivity.class);
                        }
                        intent.putExtra("openAddDialog", true);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}
