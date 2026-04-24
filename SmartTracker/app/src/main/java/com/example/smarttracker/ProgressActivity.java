package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ProgressActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private static final String PREF_NAME = "smarttracker";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    BottomNavigationView bottomNav;
    FloatingActionButton fabAdd;
    ImageView ivProfile;
    TextView tvUserName;
    TextView tvProgressHabits, tvProgressWorkouts;
    TextView tvWeeklyPercent, tvWeeklyMessage;
    ProgressBar progressWeeklyBar, progressLoading;
    LinearLayout layoutDailyBars;
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

        setContentView(R.layout.activity_progress);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvProgressHabits = (TextView) findViewById(R.id.tvProgressHabits);
        tvProgressWorkouts = (TextView) findViewById(R.id.tvProgressWorkouts);
        tvWeeklyPercent = (TextView) findViewById(R.id.tvWeeklyPercent);
        tvWeeklyMessage = (TextView) findViewById(R.id.tvWeeklyMessage);
        progressWeeklyBar = (ProgressBar) findViewById(R.id.progressWeeklyBar);
        progressLoading = (ProgressBar) findViewById(R.id.progressLoading);
        layoutDailyBars = (LinearLayout) findViewById(R.id.layoutDailyBars);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        tvUserName.setText(prefs.getString(KEY_USER_NAME, "User"));

        bottomNav.setSelectedItemId(R.id.nav_progress);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_progress) {
                    return true;
                } else if (id == R.id.nav_home) {
                    startActivity(new Intent(ProgressActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(ProgressActivity.this, HabitsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(ProgressActivity.this, WorkoutsActivity.class));
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
                showAddChooser();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProgressActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + prefs.getString(KEY_USER_EMAIL, ""))
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().apply();
                                startActivity(new Intent(ProgressActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) return;
        loadProgress();
    }

    private void loadProgress() {
        progressLoading.setVisibility(View.VISIBLE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        String url = BASE_URL + "getprogress.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressLoading.setVisibility(View.GONE);

                        try {
                            JSONObject json = new JSONObject(response);

                            long hDone = json.getLong("habitsCompleted");
                            long hTotal = json.getLong("habitsTotal");
                            long wDone = json.getLong("workoutsCompleted");
                            long wTotal = json.getLong("workoutsTotal");
                            double pct = json.getDouble("weeklyProgressPercent");

                            tvProgressHabits.setText(hDone + " / " + hTotal);
                            tvProgressWorkouts.setText(wDone + " / " + wTotal);

                            int pctInt = (int) pct;
                            tvWeeklyPercent.setText(pctInt + "%");
                            progressWeeklyBar.setProgress(pctInt);

                            if (pct >= 80) {
                                tvWeeklyMessage.setText("Outstanding! You're crushing your goals!");
                            } else if (pct >= 50) {
                                tvWeeklyMessage.setText("Great progress! Keep the momentum going.");
                            } else if (pct > 0) {
                                tvWeeklyMessage.setText("You've started - now push toward 50%!");
                            } else {
                                tvWeeklyMessage.setText("Complete some tasks to see your progress here.");
                            }

                            layoutDailyBars.removeAllViews();
                            JSONObject daily = json.getJSONObject("dailyProgress");
                            Iterator<String> keys = daily.keys();

                            while (keys.hasNext()) {
                                String dayName = keys.next();
                                double dayPct = daily.getDouble(dayName);

                                View barView = LayoutInflater.from(ProgressActivity.this)
                                        .inflate(R.layout.item_daily_bar, layoutDailyBars, false);

                                TextView tvDay = (TextView) barView.findViewById(R.id.tvDayName);
                                ProgressBar progressDay = (ProgressBar) barView.findViewById(R.id.progressDay);
                                TextView tvPercent = (TextView) barView.findViewById(R.id.tvDayPercent);

                                tvDay.setText(dayName.substring(0, 3).toUpperCase());
                                progressDay.setProgress((int) dayPct);
                                tvPercent.setText((int) dayPct + "%");

                                layoutDailyBars.addView(barView);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(ProgressActivity.this,
                                "Failed to load progress", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }

    private void showAddChooser() {
        new AlertDialog.Builder(this)
                .setTitle("Add New")
                .setItems(new String[]{"New Habit", "New Workout"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            startActivity(new Intent(ProgressActivity.this, HabitsActivity.class)
                                    .putExtra("openAddDialog", true));
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            startActivity(new Intent(ProgressActivity.this, WorkoutsActivity.class)
                                    .putExtra("openAddDialog", true));
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    }
                })
                .show();
    }
}
