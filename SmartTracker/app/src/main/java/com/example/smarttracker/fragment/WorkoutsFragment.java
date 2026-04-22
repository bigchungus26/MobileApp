package com.example.smarttracker.fragment;

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

import com.example.smarttracker.R;
import com.example.smarttracker.adapter.WorkoutAdapter;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.data.Workout;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class WorkoutsFragment extends Fragment implements WorkoutAdapter.OnWorkoutActionListener {

    private RecyclerView recyclerWorkouts;
    private WorkoutAdapter workoutAdapter;
    private ProgressBar progressWorkouts;
    private TextView tvEmpty;
    private Repository repository;
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
        repository = Repository.get(requireContext());
        sessionManager = new SessionManager(requireContext());

        recyclerWorkouts = view.findViewById(R.id.recyclerWorkouts);
        progressWorkouts = view.findViewById(R.id.progressWorkouts);
        tvEmpty = view.findViewById(R.id.tvEmptyWorkouts);

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

    private void loadWorkouts() {
        progressWorkouts.setVisibility(View.GONE);
        List<Workout> workouts = repository.getTodayWorkouts(sessionManager.getUserId());
        workoutAdapter.setWorkouts(workouts);
        tvEmpty.setVisibility(workouts.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerWorkouts.setVisibility(workouts.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_workout, null);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etWorkoutTitle);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        TextInputEditText etCalories = dialogView.findViewById(R.id.etCalories);
        RadioGroup rgIntensity = dialogView.findViewById(R.id.rgIntensity);

        new AlertDialog.Builder(requireContext())
                .setTitle("Log Workout")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int duration = parseIntOrZero(etDuration.getText().toString().trim());
                    int calories = parseIntOrZero(etCalories.getText().toString().trim());

                    String intensity;
                    if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                        intensity = "LOW";
                    } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                        intensity = "HIGH";
                    } else {
                        intensity = "MEDIUM";
                    }

                    repository.addWorkout(sessionManager.getUserId(), title, duration,
                            calories, intensity);
                    Toast.makeText(requireContext(), "Workout logged!", Toast.LENGTH_SHORT).show();
                    loadWorkouts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int parseIntOrZero(String s) {
        if (s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onToggle(int workoutId) {
        repository.toggleWorkout(workoutId, sessionManager.getUserId());
        loadWorkouts();
    }

    @Override
    public void onDelete(int workoutId, String title) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteWorkout(workoutId, sessionManager.getUserId());
                    loadWorkouts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
