package com.example.smarttracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smarttracker.R;
import com.example.smarttracker.api.ApiClient;
import com.example.smarttracker.api.ApiService;
import com.example.smarttracker.model.ProgressResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressFragment extends Fragment {

    private TextView tvProgressHabits, tvProgressWorkouts;
    private TextView tvWeeklyPercent, tvWeeklyMessage;
    private ProgressBar progressWeeklyBar, progressLoading;
    private LinearLayout layoutDailyBars;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = ApiClient.getApiService(requireContext());

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

    private void loadProgress() {
        progressLoading.setVisibility(View.VISIBLE);
        api.getProgress().enqueue(new Callback<ProgressResponse>() {
            @Override
            public void onResponse(Call<ProgressResponse> call, Response<ProgressResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProgressResponse p = response.body();

                    tvProgressHabits.setText(p.getHabitsCompleted() + " / " + p.getHabitsTotal());
                    tvProgressWorkouts.setText(p.getWorkoutsCompleted() + " / " + p.getWorkoutsTotal());

                    int pct = (int) p.getWeeklyProgressPercent();
                    tvWeeklyPercent.setText(pct + "%");
                    progressWeeklyBar.setProgress(pct);

                    if (pct >= 80) {
                        tvWeeklyMessage.setText("Outstanding! You're crushing your goals!");
                    } else if (pct >= 50) {
                        tvWeeklyMessage.setText("Great progress! Keep the momentum going.");
                    } else if (pct > 0) {
                        tvWeeklyMessage.setText("You've started — now push toward 50%!");
                    } else {
                        tvWeeklyMessage.setText("Complete some tasks to see your progress here.");
                    }

                    // Build daily bars
                    layoutDailyBars.removeAllViews();
                    Map<String, Double> daily = p.getDailyProgress();
                    if (daily != null) {
                        for (Map.Entry<String, Double> entry : daily.entrySet()) {
                            View barView = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.item_daily_bar, layoutDailyBars, false);

                            TextView tvDay = barView.findViewById(R.id.tvDayName);
                            ProgressBar progressDay = barView.findViewById(R.id.progressDay);
                            TextView tvPercent = barView.findViewById(R.id.tvDayPercent);

                            String dayName = entry.getKey().substring(0, 3);
                            tvDay.setText(dayName);
                            progressDay.setProgress(entry.getValue().intValue());
                            tvPercent.setText(entry.getValue().intValue() + "%");

                            layoutDailyBars.addView(barView);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProgressResponse> call, Throwable t) {
                if (isAdded()) {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load progress", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
