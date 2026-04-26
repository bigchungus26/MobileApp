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
import com.example.smarttracker.adapter.HabitAdapter;
import com.example.smarttracker.data.Habit;
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

public class HabitsActivity extends AppCompatActivity implements HabitAdapter.OnHabitDeleteListener {

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressHabits;
    RecyclerView recyclerHabits;
    HabitAdapter habitAdapter;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;
    SessionManager sessionManager;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(HabitsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_habits);

        queue = Volley.newRequestQueue(HabitsActivity.this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressHabits = (ProgressBar) findViewById(R.id.progressHabits);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyHabits);
        recyclerHabits = (RecyclerView) findViewById(R.id.recyclerHabits);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        habitAdapter = new HabitAdapter(HabitsActivity.this);
        recyclerHabits.setLayoutManager(new LinearLayoutManager(HabitsActivity.this));
        recyclerHabits.setAdapter(habitAdapter);

        bottomNav.setSelectedItemId(R.id.nav_habits);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_habits) {
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(HabitsActivity.this, MainActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(HabitsActivity.this, WorkoutsActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(HabitsActivity.this, ProgressActivity.class));
                    finish();
                    return false;
                }
                return false;
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAddHabitDialog(); }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HabitsActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + sessionManager.getUserEmail())
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sessionManager.logout();
                                startActivity(new Intent(HabitsActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        loadHabits();

        if (getIntent().getBooleanExtra("openAddDialog", false)) {
            showAddHabitDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadHabits();
        }
    }

    private void loadHabits() {
        progressHabits.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = ApiConfig.GET_HABITS + "?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressHabits.setVisibility(View.GONE);
                        List<Habit> habits = new ArrayList<>();
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                Habit h = new Habit();
                                h.id = o.getInt("id");
                                h.userId = o.optInt("user_id");
                                h.title = o.optString("title");
                                h.description = o.optString("description");
                                h.category = o.optString("category");
                                h.frequency = o.optString("frequency", "DAILY");
                                h.streak = o.optInt("streak", 0);
                                h.active = o.optInt("active", 1) == 1;
                                habits.add(h);
                            }
                        } catch (Exception ignored) { }

                        habitAdapter.setHabits(habits);
                        if (habits.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerHabits.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerHabits.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressHabits.setVisibility(View.GONE);
                        Toast.makeText(HabitsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    private void showAddHabitDialog() {
        View dialogView = LayoutInflater.from(HabitsActivity.this)
                .inflate(R.layout.dialog_add_habit, null);

        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etHabitTitle);
        final TextInputEditText etDescription = (TextInputEditText) dialogView.findViewById(R.id.etHabitDescription);
        final TextInputEditText etCategory = (TextInputEditText) dialogView.findViewById(R.id.etHabitCategory);
        final RadioGroup rgFrequency = (RadioGroup) dialogView.findViewById(R.id.rgFrequency);

        new AlertDialog.Builder(HabitsActivity.this)
                .setTitle("New Habit")
                .setView(dialogView)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(HabitsActivity.this,
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String description = etDescription.getText().toString().trim();
                        String category = etCategory.getText().toString().trim();
                        String frequency = rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly
                                ? "WEEKLY" : "DAILY";

                        addHabit(title, description, category, frequency);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addHabit(final String title, final String description,
                          final String category, final String frequency) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.ADD_HABIT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(HabitsActivity.this,
                                "Habit created!", Toast.LENGTH_SHORT).show();
                        loadHabits();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HabitsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("title", title);
                params.put("description", description);
                params.put("category", category);
                params.put("frequency", frequency);
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDelete(final int habitId, String title) {
        new AlertDialog.Builder(HabitsActivity.this)
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteHabit(habitId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteHabit(final int habitId) {
        final int userId = sessionManager.getUserId();

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.DELETE_HABIT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { loadHabits(); }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HabitsActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("habit_id", String.valueOf(habitId));
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        queue.add(request);
    }
}
