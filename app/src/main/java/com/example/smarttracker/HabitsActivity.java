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

import com.example.smarttracker.adapter.HabitAdapter;
import com.example.smarttracker.data.Habit;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HabitsActivity extends AppCompatActivity implements HabitAdapter.OnHabitDeleteListener {

    TextView tvUserName, tvEmpty;
    ImageView ivProfile;
    ProgressBar progressHabits;
    RecyclerView recyclerHabits;
    HabitAdapter habitAdapter;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNav;
    Repository repository;
    SessionManager sessionManager;

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

        repository = Repository.get(HabitsActivity.this);

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
        progressHabits.setVisibility(View.GONE);
        List<Habit> habits = repository.getHabits(sessionManager.getUserId());
        habitAdapter.setHabits(habits);
        if (habits.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerHabits.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerHabits.setVisibility(View.VISIBLE);
        }
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

                        String frequency;
                        if (rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly) {
                            frequency = "WEEKLY";
                        } else {
                            frequency = "DAILY";
                        }

                        repository.addHabit(sessionManager.getUserId(), title, description,
                                category, frequency);
                        Toast.makeText(HabitsActivity.this,
                                "Habit created!", Toast.LENGTH_SHORT).show();
                        loadHabits();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(final int habitId, String title) {
        new AlertDialog.Builder(HabitsActivity.this)
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        repository.deleteHabit(habitId, sessionManager.getUserId());
                        loadHabits();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
