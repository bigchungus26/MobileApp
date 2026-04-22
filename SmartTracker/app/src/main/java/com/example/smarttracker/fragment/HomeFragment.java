package com.example.smarttracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.adapter.TaskAdapter;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskToggleListener {

    private TextView tvHabitsCount, tvWorkoutCount, tvProgressText;
    private ProgressBar progressWeekly;
    private RecyclerView recyclerTodayTasks;
    private TaskAdapter taskAdapter;
    private Repository repository;
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
        repository = Repository.get(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvHabitsCount = view.findViewById(R.id.tvHabitsCount);
        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvProgressText = view.findViewById(R.id.tvProgressText);
        progressWeekly = view.findViewById(R.id.progressWeekly);
        recyclerTodayTasks = view.findViewById(R.id.recyclerTodayTasks);

        Button btnStartNow = view.findViewById(R.id.btnStartNow);
        btnStartNow.setOnClickListener(v -> {
            View fab = requireActivity().findViewById(R.id.fabAdd);
            if (fab != null) fab.performClick();
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
        int userId = sessionManager.getUserId();
        taskAdapter.setTasks(repository.getTodayTasks(userId));

        Repository.ProgressSummary p = repository.getProgress(userId);
        tvHabitsCount.setText(p.habitsCompleted + " / " + p.habitsTotal);
        tvWorkoutCount.setText(p.workoutsCompleted + " / " + p.workoutsTotal);
        progressWeekly.setProgress((int) p.weeklyPercent);

        if (p.weeklyPercent >= 80) {
            tvProgressText.setText("Amazing! You're crushing it this week!");
        } else if (p.weeklyPercent >= 50) {
            tvProgressText.setText("You're doing great. Keep your streak going.");
        } else if (p.weeklyPercent > 0) {
            tvProgressText.setText("Good start! Keep pushing to hit your goals.");
        } else {
            tvProgressText.setText("Start completing tasks to see your progress.");
        }
    }

    @Override
    public void onToggle(int taskId) {
        repository.toggleTask(taskId, sessionManager.getUserId());
        loadData();
    }
}
