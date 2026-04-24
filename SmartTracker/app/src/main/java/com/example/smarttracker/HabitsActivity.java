package com.example.smarttracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class HabitsActivity extends AppCompatActivity implements HabitAdapter.OnHabitDeleteListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private static final String PREF_NAME = "smarttracker";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    BottomNavigationView bottomNav;
    FloatingActionButton fabAdd;
    ImageView ivProfile;
    TextView tvUserName, tvEmpty;
    ProgressBar progressHabits;
    RecyclerView recyclerHabits;
    HabitAdapter habitAdapter;
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

        setContentView(R.layout.activity_habits);

        queue = Volley.newRequestQueue(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        recyclerHabits = (RecyclerView) findViewById(R.id.recyclerHabits);
        progressHabits = (ProgressBar) findViewById(R.id.progressHabits);
        tvEmpty = (TextView) findViewById(R.id.tvEmptyHabits);
        bottomNav = (BottomNavigationView) findViewById(R.id.bottomNav);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);

        tvUserName.setText(prefs.getString(KEY_USER_NAME, "User"));

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
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_workouts) {
                    startActivity(new Intent(HabitsActivity.this, WorkoutsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_progress) {
                    startActivity(new Intent(HabitsActivity.this, ProgressActivity.class));
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
                showAddHabitDialog();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HabitsActivity.this)
                        .setTitle("Account")
                        .setMessage("Logged in as " + prefs.getString(KEY_USER_EMAIL, ""))
                        .setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().apply();
                                startActivity(new Intent(HabitsActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        if (getIntent().getBooleanExtra("openAddDialog", false)) {
            showAddHabitDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) return;
        loadHabits();
    }

    private void loadHabits() {
        progressHabits.setVisibility(View.VISIBLE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        String url = BASE_URL + "gethabits.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressHabits.setVisibility(View.GONE);
                        habitAdapter.setHabits(response);
                        tvEmpty.setVisibility(response.length() == 0 ? View.VISIBLE : View.GONE);
                        recyclerHabits.setVisibility(response.length() == 0 ? View.GONE : View.VISIBLE);
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
                        final String frequency = rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly
                                ? "WEEKLY" : "DAILY";

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
                                params.put("user_id", String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
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
    public void onDelete(int habitId, String title) {
        final int hId = habitId;
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
                                params.put("habit_id", String.valueOf(hId));
                                params.put("user_id", String.valueOf(prefs.getInt(KEY_USER_ID, -1)));
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
