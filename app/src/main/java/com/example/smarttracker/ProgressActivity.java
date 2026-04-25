package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Map;

public class ProgressActivity extends AppCompatActivity {

    TextView tvUserName, tvProgressHabits, tvProgressWorkouts, tvWeeklyPercent, tvWeeklyMessage;
    ImageView ivProfile;
    ProgressBar progressWeeklyBar, progressLoading;
    LinearLayout layoutDailyBars;
    BottomNavigationView bottomNav;
    Repository repository;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(ProgressActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_progress);

        repository = Repository.get(ProgressActivity.this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvProgressHabits = (TextView) findViewById(R.id.tvProgressHabits);
        tvProgressWorkouts = (TextView) findViewById(R.id.tvProgressWorkouts);
        tvWeeklyPercent = (TextView) findViewById(R.id.tvWeeklyPercent);
        tvWeeklyMessage = (TextView) findViewById(R.id.tvWeeklyMessage);
        progressWeeklyBar = (ProgressBar) findViewById(R.id.progressWeeklyBar);
        progressLoading = (ProgressBar) findViewById(R.id.progressLoading);
        layoutDailyBars = (LinearLayout) findViewById(R.id.layoutDailyBars);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);

        tvUserName.setText(sessionManager.getUserName());

        bottomNav.setSelectedItemId(R.id.nav_progress);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_progress) {
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(ProgressActivity.this, MainActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(ProgressActivity.this, HabitsActivity.class));
                    finish();
                    return false;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(ProgressActivity.this, WorkoutsActivity.class));
                    finish();
                    return false;
                }
                return false;
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProgressActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + sessionManager.getUserEmail())
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sessionManager.logout();
                                startActivity(new Intent(ProgressActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        loadProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadProgress();
        }
    }

    private void loadProgress() {
        progressLoading.setVisibility(View.GONE);

        Repository.ProgressSummary p = repository.getProgress(sessionManager.getUserId());

        tvProgressHabits.setText(p.habitsCompleted + " / " + p.habitsTotal);
        tvProgressWorkouts.setText(p.workoutsCompleted + " / " + p.workoutsTotal);

        int pctInt = (int) p.weeklyPercent;
        tvWeeklyPercent.setText(pctInt + "%");
        progressWeeklyBar.setProgress(pctInt);

        if (p.weeklyPercent >= 80) {
            tvWeeklyMessage.setText("Outstanding! You're crushing your goals!");
        } else if (p.weeklyPercent >= 50) {
            tvWeeklyMessage.setText("Great progress! Keep the momentum going.");
        } else if (p.weeklyPercent > 0) {
            tvWeeklyMessage.setText("You've started — now push toward 50%!");
        } else {
            tvWeeklyMessage.setText("Complete some tasks to see your progress here.");
        }

        layoutDailyBars.removeAllViews();
        for (Map.Entry<String, Double> entry : p.daily.entrySet()) {
            View barView = LayoutInflater.from(ProgressActivity.this)
                    .inflate(R.layout.item_daily_bar, layoutDailyBars, false);

            TextView tvDay = (TextView) barView.findViewById(R.id.tvDayName);
            ProgressBar progressDay = (ProgressBar) barView.findViewById(R.id.progressDay);
            TextView tvPercent = (TextView) barView.findViewById(R.id.tvDayPercent);

            tvDay.setText(entry.getKey().substring(0, 3).toUpperCase());
            progressDay.setProgress(entry.getValue().intValue());
            tvPercent.setText(entry.getValue().intValue() + "%");

            layoutDailyBars.addView(barView);
        }
    }
}
