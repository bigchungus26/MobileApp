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

import com.example.smarttracker.adapter.WorkoutAdapter;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.data.Workout;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class WorkoutsActivity extends AppCompatActivity implements WorkoutAdapter.OnWorkoutActionListener {

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressWorkouts;
    RecyclerView recyclerWorkouts;
    WorkoutAdapter workoutAdapter;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;
    Repository repository;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(WorkoutsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_workouts);

        repository = Repository.get(WorkoutsActivity.this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        progressWorkouts = (ProgressBar) findViewById(R.id.progressWorkouts);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyWorkouts);
        recyclerWorkouts = (RecyclerView) findViewById(R.id.recyclerWorkouts);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        workoutAdapter = new WorkoutAdapter(WorkoutsActivity.this);
        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(WorkoutsActivity.this));
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
        progressWorkouts.setVisibility(View.GONE);
        List<Workout> workouts = repository.getTodayWorkouts(sessionManager.getUserId());
        workoutAdapter.setWorkouts(workouts);
        if (workouts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerWorkouts.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerWorkouts.setVisibility(View.VISIBLE);
        }
    }

    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(WorkoutsActivity.this)
                .inflate(R.layout.dialog_add_workout, null);

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
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(WorkoutsActivity.this,
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int duration = parseIntOrZero(etDuration.getText().toString().trim());
                        int calories = parseIntOrZero(etCalories.getText().toString().trim());

                        String intensity;
                        if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                            intensity = "LOW";
                        } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                            intensity = "HIGH";
                        } else {
                            intensity = "MEDIUM";
                        }

                        repository.addWorkout(sessionManager.getUserId(), title, duration,
                                calories, intensity);
                        Toast.makeText(WorkoutsActivity.this,
                                "Workout logged!", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int parseIntOrZero(String s) {
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onToggle(int workoutId) {
        repository.toggleWorkout(workoutId, sessionManager.getUserId());
        loadWorkouts();
    }

    @Override
    public void onDelete(final int workoutId, String title) {
        new AlertDialog.Builder(WorkoutsActivity.this)
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteWorkout(workoutId, sessionManager.getUserId());
                        loadWorkouts();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
