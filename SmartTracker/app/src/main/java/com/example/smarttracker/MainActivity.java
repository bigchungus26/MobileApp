package com.example.smarttracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.smarttracker.fragment.HabitsFragment;
import com.example.smarttracker.fragment.HomeFragment;
import com.example.smarttracker.fragment.ProgressFragment;
import com.example.smarttracker.fragment.WorkoutsFragment;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    private HomeFragment homeFragment = new HomeFragment();
    private HabitsFragment habitsFragment = new HabitsFragment();
    private WorkoutsFragment workoutsFragment = new WorkoutsFragment();
    private ProgressFragment progressFragment = new ProgressFragment();
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvUserName = findViewById(R.id.tvUserName);
        bottomNav = findViewById(R.id.bottomNav);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageView ivProfile = findViewById(R.id.ivProfile);

        // Set real user name from session
        tvUserName.setText(sessionManager.getUserName());

        // Set up fragments
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, progressFragment, "progress").hide(progressFragment)
                .add(R.id.fragmentContainer, workoutsFragment, "workouts").hide(workoutsFragment)
                .add(R.id.fragmentContainer, habitsFragment, "habits").hide(habitsFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();
        activeFragment = homeFragment;

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
            } else if (id == R.id.nav_habits) {
                switchFragment(habitsFragment);
            } else if (id == R.id.nav_workouts) {
                switchFragment(workoutsFragment);
            } else if (id == R.id.nav_progress) {
                switchFragment(progressFragment);
            }
            return true;
        });

        fabAdd.setOnClickListener(v -> {
            if (activeFragment == habitsFragment) {
                habitsFragment.showAddHabitDialog();
            } else if (activeFragment == workoutsFragment) {
                workoutsFragment.showAddWorkoutDialog();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Add New")
                        .setItems(new String[]{"New Habit", "New Workout"}, (dialog, which) -> {
                            if (which == 0) {
                                bottomNav.setSelectedItemId(R.id.nav_habits);
                                habitsFragment.showAddHabitDialog();
                            } else {
                                bottomNav.setSelectedItemId(R.id.nav_workouts);
                                workoutsFragment.showAddWorkoutDialog();
                            }
                        })
                        .show();
            }
        });

        // Profile icon -> logout option
        ivProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Account")
                    .setMessage("Logged in as " + sessionManager.getUserEmail())
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(this, LoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void switchFragment(Fragment target) {
        if (target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }
}
