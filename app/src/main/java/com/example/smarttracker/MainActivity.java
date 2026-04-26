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

import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

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
    Repository repository;
    SessionManager sessionManager;

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

        repository = Repository.get(MainActivity.this);

        //show Settings menu item through actionbar
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

    //inflate the action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //handle Settings click from the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        int userId = sessionManager.getUserId();
        taskAdapter.setTasks(repository.getTodayTasks(userId));

        Repository.ProgressSummary p = repository.getProgress(userId);
        tvHabitsCount.setText(p.habitsCompleted + " / " + p.habitsTotal);
        tvWorkoutCount.setText(p.workoutsCompleted + " / " + p.workoutsTotal);
        progressWeekly.setProgress((int) p.weeklyPercent);

        if (p.weeklyPercent >= 80) {
            tvProgressText.setText("Amazing! You're crushing it this week!");
        } else if (p.weeklyPercent >= 50) {
            tvProgressText.setText("You're doing great. Keep your streak going.");
        } else if (p.weeklyPercent > 0) {
            tvProgressText.setText("Good start! Keep pushing to hit your goals.");
        } else {
            tvProgressText.setText("Start completing tasks to see your progress.");
        }
    }

    @Override
    public void onToggle(int taskId) {
        repository.toggleTask(taskId, sessionManager.getUserId());
        loadData();
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
