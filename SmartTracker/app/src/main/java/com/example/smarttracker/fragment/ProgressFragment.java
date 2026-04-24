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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.R;
import com.example.smarttracker.util.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ProgressFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private TextView tvProgressHabits, tvProgressWorkouts;
    private TextView tvWeeklyPercent, tvWeeklyMessage;
    private ProgressBar progressWeeklyBar, progressLoading;
    private LinearLayout layoutDailyBars;
    private RequestQueue queue;
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
        queue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        tvProgressHabits = (TextView) view.findViewById(R.id.tvProgressHabits);
        tvProgressWorkouts = (TextView) view.findViewById(R.id.tvProgressWorkouts);
        tvWeeklyPercent = (TextView) view.findViewById(R.id.tvWeeklyPercent);
        tvWeeklyMessage = (TextView) view.findViewById(R.id.tvWeeklyMessage);
        progressWeeklyBar = (ProgressBar) view.findViewById(R.id.progressWeeklyBar);
        progressLoading = (ProgressBar) view.findViewById(R.id.progressLoading);
        layoutDailyBars = (LinearLayout) view.findViewById(R.id.layoutDailyBars);

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
        progressLoading.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "getprogress.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;
                        progressLoading.setVisibility(View.GONE);

                        try {
                            JSONObject json = new JSONObject(response);

                            long hDone = json.getLong("habitsCompleted");
                            long hTotal = json.getLong("habitsTotal");
                            long wDone = json.getLong("workoutsCompleted");
                            long wTotal = json.getLong("workoutsTotal");
                            double pct = json.getDouble("weeklyProgressPercent");

                            tvProgressHabits.setText(hDone + " / " + hTotal);
                            tvProgressWorkouts.setText(wDone + " / " + wTotal);

                            int pctInt = (int) pct;
                            tvWeeklyPercent.setText(pctInt + "%");
                            progressWeeklyBar.setProgress(pctInt);

                            if (pct >= 80) {
                                tvWeeklyMessage.setText("Outstanding! You're crushing your goals!");
                            } else if (pct >= 50) {
                                tvWeeklyMessage.setText("Great progress! Keep the momentum going.");
                            } else if (pct > 0) {
                                tvWeeklyMessage.setText("You've started - now push toward 50%!");
                            } else {
                                tvWeeklyMessage.setText("Complete some tasks to see your progress here.");
                            }

                            layoutDailyBars.removeAllViews();
                            JSONObject daily = json.getJSONObject("dailyProgress");
                            Iterator<String> keys = daily.keys();

                            while (keys.hasNext()) {
                                String dayName = keys.next();
                                double dayPct = daily.getDouble(dayName);

                                View barView = LayoutInflater.from(requireContext())
                                        .inflate(R.layout.item_daily_bar, layoutDailyBars, false);

                                TextView tvDay = (TextView) barView.findViewById(R.id.tvDayName);
                                ProgressBar progressDay = (ProgressBar) barView.findViewById(R.id.progressDay);
                                TextView tvPercent = (TextView) barView.findViewById(R.id.tvDayPercent);

                                tvDay.setText(dayName.substring(0, 3).toUpperCase());
                                progressDay.setProgress((int) dayPct);
                                tvPercent.setText((int) dayPct + "%");

                                layoutDailyBars.addView(barView);
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
                            progressLoading.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    "Failed to load progress", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);
    }
}
