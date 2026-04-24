package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    FloatingActionButton fabAdd;
    ImageView ivProfile;
    TextView tvUserName;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("smarttracker", MODE_PRIVATE);
        if (prefs.getInt("user_id", -1) == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        tvUserName.setText(prefs.getString("user_name", "User"));

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_habits) {
                    startActivity(new Intent(MainActivity.this, HabitsActivity.class));
                    return false;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(MainActivity.this, WorkoutsActivity.class));
                    return false;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(MainActivity.this, ProgressActivity.class));
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

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + prefs.getString("user_email", ""))
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().apply();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    public void navigateToTab(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    public void showAddChooser() {
        new AlertDialog.Builder(this)
                .setTitle("Add New")
                .setItems(new String[]{"New Habit", "New Workout"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            startActivity(new Intent(MainActivity.this, HabitsActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, WorkoutsActivity.class));
                        }
                    }
                })
                .show();
    }
}
