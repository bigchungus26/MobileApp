package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.smarttracker.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    //👉 views + prefs for this screen
    BottomNavigationView bottomNav;
    TextView tvUserName;
    ImageView ivProfile;
    FloatingActionButton fabAdd;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if not logged in, go to login screen
        prefs = getSharedPreferences("smarttracker", MODE_PRIVATE);
        if (prefs.getInt("user_id", -1) == -1) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(MainActivity.this);
        setContentView(R.layout.activity_main);

        //hook up the action bar so we can show the Settings menu item
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

        //get user name through sharedPreferences
        tvUserName.setText(prefs.getString("user_name", "User"));

        if (savedInstanceState == null) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.replace(R.id.fragmentContainer, new HomeFragment());
            tx.commit();
        }

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

    //inflate the action bar menu (Settings item)
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
