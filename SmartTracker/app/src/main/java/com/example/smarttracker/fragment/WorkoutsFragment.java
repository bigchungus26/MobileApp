package com.example.smarttracker.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.R;
import com.example.smarttracker.adapter.WorkoutAdapter;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class WorkoutsFragment extends Fragment implements WorkoutAdapter.OnWorkoutActionListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private RecyclerView recyclerWorkouts;
    private WorkoutAdapter workoutAdapter;
    private ProgressBar progressWorkouts;
    private TextView tvEmpty;
    private RequestQueue queue;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        recyclerWorkouts = (RecyclerView) view.findViewById(R.id.recyclerWorkouts);
        progressWorkouts = (ProgressBar) view.findViewById(R.id.progressWorkouts);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmptyWorkouts);

        workoutAdapter = new WorkoutAdapter(this);
        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerWorkouts.setAdapter(workoutAdapter);

        loadWorkouts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWorkouts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadWorkouts();
    }

    private void loadWorkouts() {
        progressWorkouts.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "getworkouts.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (!isAdded()) return;
                        progressWorkouts.setVisibility(View.GONE);
                        workoutAdapter.setWorkouts(response);
                        tvEmpty.setVisibility(response.length() == 0 ? View.VISIBLE : View.GONE);
                        recyclerWorkouts.setVisibility(response.length() == 0 ? View.GONE : View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            progressWorkouts.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    "Failed to load workouts", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);
    }

    public void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_workout, null);

        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etWorkoutTitle);
        final TextInputEditText etDuration = (TextInputEditText) dialogView.findViewById(R.id.etDuration);
        final TextInputEditText etCalories = (TextInputEditText) dialogView.findViewById(R.id.etCalories);
        final RadioGroup rgIntensity = (RadioGroup) dialogView.findViewById(R.id.rgIntensity);

        new AlertDialog.Builder(requireContext())
                .setTitle("Log Workout")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final String duration = etDuration.getText().toString().trim();
                        final String calories = etCalories.getText().toString().trim();

                        final String intensity;
                        if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                            intensity = "LOW";
                        } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                            intensity = "HIGH";
                        } else {
                            intensity = "MEDIUM";
                        }

                        String url = BASE_URL + "addworkout.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Workout logged!", Toast.LENGTH_SHORT).show();
                                            loadWorkouts();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Network error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                params.put("title", title);
                                params.put("duration_minutes", duration.isEmpty() ? "0" : duration);
                                params.put("calories", calories.isEmpty() ? "0" : calories);
                                params.put("intensity", intensity);
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
    public void onToggle(int workoutId) {
        String url = BASE_URL + "toggleworkout.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isAdded()) loadWorkouts();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("workout_id", String.valueOf(workoutId));
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onDelete(int workoutId, String title) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = BASE_URL + "deleteworkout.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (isAdded()) loadWorkouts();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("workout_id", String.valueOf(workoutId));
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
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
