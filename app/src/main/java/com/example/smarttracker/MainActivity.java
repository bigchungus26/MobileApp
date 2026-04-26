package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.data.Task;
import com.example.smarttracker.util.ApiConfig;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskToggleListener {

    BottomNavigationView bottomNav;
    TextView tvUserName, tvHabitsCount, tvWorkoutCount, tvProgressText;
    ImageView ivProfile;
    ProgressBar progressWeekly;
    RecyclerView recyclerTodayTasks;
    FloatingActionButton fabAdd;
    Button btnStartNow;
    TextView tvSeeAll;
    TaskAdapter taskAdapter;
    SessionManager sessionManager;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(MainActivity.this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(MainActivity.this);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(MainActivity.this);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SmartTracker");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvHabitsCount = (TextView) findViewById(R.id.tvHabitsCount);
        tvWorkoutCount = (TextView) findViewById(R.id.tvWorkoutCount);
        tvProgressText = (TextView) findViewById(R.id.tvProgressText);
        progressWeekly = (ProgressBar) findViewById(R.id.progressWeekly);
        recyclerTodayTasks = (RecyclerView) findViewById(R.id.recyclerTodayTasks);
        btnStartNow = (Button) findViewById(R.id.btnStartNow);
        tvSeeAll = (TextView) findViewById(R.id.tvSeeAll);

        tvUserName.setText(sessionManager.getUserName());

        taskAdapter = new TaskAdapter(MainActivity.this);
        recyclerTodayTasks.setLayoutManager(new LinearLayoutManager(MainActivity.this));
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
            public void onClick(View v) { showAddChooser(); }
        });

        btnStartNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAddChooser(); }
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

        loadTasks();
        loadProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadTasks();
            loadProgress();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTasks() {
        final int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_TASKS + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        List<Task> tasks = new ArrayList<>();
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                Task t = new Task();
                                t.id = o.getInt("id");
                                t.title = o.optString("title");
                                t.description = o.optString("description");
                                t.completed = o.optInt("completed", 0) == 1;
                                tasks.add(t);
                            }
                        } catch (Exception ignored) { }
                        taskAdapter.setTasks(tasks);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                });

        queue.add(request);
    }

    private void loadProgress() {
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_PROGRESS + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            int hDone = json.optInt("habitsCompleted", 0);
                            int hTotal = json.optInt("habitsTotal", 0);
                            int wDone = json.optInt("workoutsCompleted", 0);
                            int wTotal = json.optInt("workoutsTotal", 0);
                            double weekly = json.optDouble("weeklyPercent", 0.0);

                            tvHabitsCount.setText(hDone + " / " + hTotal);
                            tvWorkoutCount.setText(wDone + " / " + wTotal);
                            progressWeekly.setProgress((int) weekly);

                            if (weekly >= 80) {
                                tvProgressText.setText("Amazing! You're crushing it this week!");
                            } else if (weekly >= 50) {
                                tvProgressText.setText("You're doing great. Keep your streak going.");
                            } else if (weekly > 0) {
                                tvProgressText.setText("Good start! Keep pushing to hit your goals.");
                            } else {
                                tvProgressText.setText("Start completing tasks to see your progress.");
                            }
                        } catch (Exception ignored) { }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                });

        queue.add(request);
    }

    @Override
    public void onToggle(final int taskId) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.TOGGLE_TASK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadTasks();
                        loadProgress();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("task_id", String.valueOf(taskId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        queue.add(request);
    }

    public void showAddChooser() {
        new AlertDialog.Builder(MainActivity.this)
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
