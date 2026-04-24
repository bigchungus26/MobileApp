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
import com.example.smarttracker.adapter.HabitAdapter;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class HabitsActivity extends AppCompatActivity implements HabitAdapter.OnHabitDeleteListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressHabits;
    RecyclerView recyclerHabits;
    HabitAdapter habitAdapter;
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

        setContentView(R.layout.activity_habits);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressHabits = (ProgressBar) findViewById(R.id.progressHabits);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyHabits);
        recyclerHabits = (RecyclerView) findViewById(R.id.recyclerHabits);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        habitAdapter = new HabitAdapter(this);
        recyclerHabits.setLayoutManager(new LinearLayoutManager(this));
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
            public void onClick(View v) {
                showAddHabitDialog();
            }
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
        String url = BASE_URL + "gethabits.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressHabits.setVisibility(View.GONE);
                        habitAdapter.setHabits(response);
                        if (response.length() == 0) {
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
                                "Failed to load habits", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    private void showAddHabitDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null);

        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etHabitTitle);
        final TextInputEditText etDescription = (TextInputEditText) dialogView.findViewById(R.id.etHabitDescription);
        final TextInputEditText etCategory = (TextInputEditText) dialogView.findViewById(R.id.etHabitCategory);
        final RadioGroup rgFrequency = (RadioGroup) dialogView.findViewById(R.id.rgFrequency);

        new AlertDialog.Builder(this)
                .setTitle("New Habit")
                .setView(dialogView)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(HabitsActivity.this,
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final String description = etDescription.getText().toString().trim();
                        final String category = etCategory.getText().toString().trim();

                        final String frequency;
                        if (rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly) {
                            frequency = "WEEKLY";
                        } else {
                            frequency = "DAILY";
                        }

                        String url = BASE_URL + "addhabit.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
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
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                params.put("title", title);
                                params.put("description", description);
                                params.put("category", category);
                                params.put("frequency", frequency);
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
    public void onDelete(final int habitId, String title) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = BASE_URL + "deletehabit.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        loadHabits();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(HabitsActivity.this,
                                                "Failed to delete", Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("habit_id", String.valueOf(habitId));
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
