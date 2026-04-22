package com.example.smarttracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smarttracker.R;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;

import java.util.Map;

public class ProgressFragment extends Fragment {

    private TextView tvProgressHabits, tvProgressWorkouts;
    private TextView tvWeeklyPercent, tvWeeklyMessage;
    private ProgressBar progressWeeklyBar, progressLoading;
    private LinearLayout layoutDailyBars;
    private Repository repository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = Repository.get(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvProgressHabits = view.findViewById(R.id.tvProgressHabits);
        tvProgressWorkouts = view.findViewById(R.id.tvProgressWorkouts);
        tvWeeklyPercent = view.findViewById(R.id.tvWeeklyPercent);
        tvWeeklyMessage = view.findViewById(R.id.tvWeeklyMessage);
        progressWeeklyBar = view.findViewById(R.id.progressWeeklyBar);
        progressLoading = view.findViewById(R.id.progressLoading);
        layoutDailyBars = view.findViewById(R.id.layoutDailyBars);

        loadProgress();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProgress();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadProgress();
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
            View barView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_daily_bar, layoutDailyBars, false);

            TextView tvDay = barView.findViewById(R.id.tvDayName);
            ProgressBar progressDay = barView.findViewById(R.id.progressDay);
            TextView tvPercent = barView.findViewById(R.id.tvDayPercent);

            tvDay.setText(entry.getKey().substring(0, 3).toUpperCase());
            progressDay.setProgress(entry.getValue().intValue());
            tvPercent.setText(entry.getValue().intValue() + "%");

            layoutDailyBars.addView(barView);
        }
    }
}
