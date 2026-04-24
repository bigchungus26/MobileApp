package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    //👉 views + helpers for this screen
    BottomNavigationView bottomNav;
    TextView tvUserName;
    ImageView ivProfile;
    FloatingActionButton fabAdd;
    SessionManager sessionManager;

    //👉 bottom-nav fragments — we add them once and just hide/show 👀
    Fragment activeFragment;
    HomeFragment homeFragment = new HomeFragment();
    HabitsFragment habitsFragment = new HabitsFragment();
    WorkoutsFragment workoutsFragment = new WorkoutsFragment();
    ProgressFragment progressFragment = new ProgressFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //👉 if nobody's logged in we bounce them to the login screen first
        sessionManager = new SessionManager(MainActivity.this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(MainActivity.this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //✅ here's the view wiring — explicit casts so we can see the types 🧵
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        //👉 slot the real user name from the saved session
        tvUserName.setText(sessionManager.getUserName());

        //👉 add every fragment up front, hide everyone except Home
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, progressFragment, "progress").hide(progressFragment)
                .add(R.id.fragmentContainer, workoutsFragment, "workouts").hide(workoutsFragment)
                .add(R.id.fragmentContainer, habitsFragment, "habits").hide(habitsFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();
        activeFragment = homeFragment;

        //👉 bottom-nav listener — anonymous inner class flavour 🍦
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
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
            }
        });
        /* lambda version — same behaviour, just shorter:
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) switchFragment(homeFragment);
            else if (id == R.id.nav_habits) switchFragment(habitsFragment);
            else if (id == R.id.nav_workouts) switchFragment(workoutsFragment);
            else if (id == R.id.nav_progress) switchFragment(progressFragment);
            return true;
        });
        */

        //✅ FAB — lambda flavour (prof said mix 'em up 🌮)
        fabAdd.setOnClickListener(v -> {
            if (activeFragment == habitsFragment) {
                habitsFragment.showAddHabitDialog();
            } else if (activeFragment == workoutsFragment) {
                workoutsFragment.showAddWorkoutDialog();
            } else {
                showAddChooser();
            }
        });
        /* anonymous version:
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (activeFragment == habitsFragment) habitsFragment.showAddHabitDialog();
                else if (activeFragment == workoutsFragment) workoutsFragment.showAddWorkoutDialog();
                else showAddChooser();
            }
        });
        */

        //👉 profile icon → little log-out dialog
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
    }

    public void navigateToTab(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    private void switchFragment(Fragment target) {
        if (target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }

    public void showAddChooser() {
        new AlertDialog.Builder(MainActivity.this)
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
}
