package com.example.smarttracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.smarttracker.MainActivity;
import com.example.smarttracker.R;
import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskToggleListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private TextView tvHabitsCount, tvWorkoutCount, tvProgressText;
    private ProgressBar progressWeekly;
    private RecyclerView recyclerTodayTasks;
    private TaskAdapter taskAdapter;
    private RequestQueue queue;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvHabitsCount = (TextView) view.findViewById(R.id.tvHabitsCount);
        tvWorkoutCount = (TextView) view.findViewById(R.id.tvWorkoutCount);
        tvProgressText = (TextView) view.findViewById(R.id.tvProgressText);
        progressWeekly = (ProgressBar) view.findViewById(R.id.progressWeekly);
        recyclerTodayTasks = (RecyclerView) view.findViewById(R.id.recyclerTodayTasks);

        Button btnStartNow = (Button) view.findViewById(R.id.btnStartNow);
        TextView tvSeeAll = (TextView) view.findViewById(R.id.tvSeeAll);

        btnStartNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddChooser();
                }
            }
        });

        tvSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_habits);
                }
            }
        });

        taskAdapter = new TaskAdapter(this);
        recyclerTodayTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTodayTasks.setAdapter(taskAdapter);

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadData();
    }

    private void loadData() {
        loadTasks();
        loadProgress();
    }

    private void loadTasks() {
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "gettasks.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (isAdded()) {
                            taskAdapter.setTasks(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to load tasks", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);
    }

    private void loadProgress() {
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "getprogress.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;
                        try {
                            JSONObject json = new JSONObject(response);
                            long hDone = json.getLong("habitsCompleted");
                            long hTotal = json.getLong("habitsTotal");
                            long wDone = json.getLong("workoutsCompleted");
                            long wTotal = json.getLong("workoutsTotal");
                            double pct = json.getDouble("weeklyProgressPercent");

                            tvHabitsCount.setText(hDone + " / " + hTotal);
                            tvWorkoutCount.setText(wDone + " / " + wTotal);
                            progressWeekly.setProgress((int) pct);

                            if (pct >= 80) {
                                tvProgressText.setText("Amazing! You're crushing it this week!");
                            } else if (pct >= 50) {
                                tvProgressText.setText("You're doing great. Keep your streak going.");
                            } else if (pct > 0) {
                                tvProgressText.setText("Good start! Keep pushing to hit your goals.");
                            } else {
                                tvProgressText.setText("Start completing tasks to see your progress.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            tvHabitsCount.setText("0 / 0");
                            tvWorkoutCount.setText("0 / 0");
                        }
                    }
                });

        queue.add(request);
    }

    @Override
    public void onToggle(int taskId) {
        String url = BASE_URL + "toggletask.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (isAdded()) loadData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Failed to update task", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("task_id", String.valueOf(taskId));
                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                return params;
            }
        };

        queue.add(request);
    }
}
