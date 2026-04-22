package com.example.smarttracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.api.ApiClient;
import com.example.smarttracker.api.ApiService;
import com.example.smarttracker.model.ProgressResponse;
import com.example.smarttracker.model.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskToggleListener {

    private TextView tvHabitsCount, tvWorkoutCount, tvProgressText;
    private ProgressBar progressWeekly;
    private RecyclerView recyclerTodayTasks;
    private TaskAdapter taskAdapter;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = ApiClient.getApiService(requireContext());

        tvHabitsCount = view.findViewById(R.id.tvHabitsCount);
        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvProgressText = view.findViewById(R.id.tvProgressText);
        progressWeekly = view.findViewById(R.id.progressWeekly);
        recyclerTodayTasks = view.findViewById(R.id.recyclerTodayTasks);

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

    private void loadData() {
        loadTasks();
        loadProgress();
    }

    private void loadTasks() {
        api.getTodayTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    taskAdapter.setTasks(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadProgress() {
        api.getProgress().enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    ProgressResponse p = response.body();
                    tvHabitsCount.setText(p.getHabitsCompleted() + " / " + p.getHabitsTotal());
                    tvWorkoutCount.setText(p.getWorkoutsCompleted() + " / " + p.getWorkoutsTotal());
                    progressWeekly.setProgress((int) p.getWeeklyProgressPercent());

                    double pct = p.getWeeklyProgressPercent();
                    if (pct >= 80) {
                        tvProgressText.setText("Amazing! You're crushing it this week!");
                    } else if (pct >= 50) {
                        tvProgressText.setText("You're doing great. Keep your streak going.");
                    } else if (pct > 0) {
                        tvProgressText.setText("Good start! Keep pushing to hit your goals.");
                    } else {
                        tvProgressText.setText("Start completing tasks to see your progress.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                if (isAdded()) {
                    tvHabitsCount.setText("0 / 0");
                    tvWorkoutCount.setText("0 / 0");
                }
            }
        });
    }

    @Override
    public void onToggle(Task task) {
        api.toggleTask(task.getId()).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (isAdded()) {
                    loadData();
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
