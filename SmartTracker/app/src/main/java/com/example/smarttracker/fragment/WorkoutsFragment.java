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
import com.example.smarttracker.api.ApiClient;
import com.example.smarttracker.api.ApiService;
import com.example.smarttracker.model.Workout;
import com.example.smarttracker.model.WorkoutRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutsFragment extends Fragment implements WorkoutAdapter.OnWorkoutActionListener {

    private RecyclerView recyclerWorkouts;
    private WorkoutAdapter workoutAdapter;
    private ProgressBar progressWorkouts;
    private TextView tvEmpty;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = ApiClient.getApiService(requireContext());

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
        progressWorkouts.setVisibility(View.VISIBLE);
        api.getTodayWorkouts().enqueue(new Callback<List<Workout>>() {
            @Override
            public void onResponse(Call<List<Workout>> call, Response<List<Workout>> response) {
                if (!isAdded()) return;
                progressWorkouts.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Workout> workouts = response.body();
                    workoutAdapter.setWorkouts(workouts);
                    tvEmpty.setVisibility(workouts.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerWorkouts.setVisibility(workouts.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Workout>> call, Throwable t) {
                if (isAdded()) {
                    progressWorkouts.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load workouts", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                    int duration = 0;
                    int calories = 0;
                    try {
                        duration = Integer.parseInt(etDuration.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}
                    try {
                        calories = Integer.parseInt(etCalories.getText().toString().trim());
                    } catch (NumberFormatException ignored) {}

                    String intensity;
                    if (rgIntensity.getCheckedRadioButtonId() == R.id.rbLow) {
                        intensity = "LOW";
                    } else if (rgIntensity.getCheckedRadioButtonId() == R.id.rbHigh) {
                        intensity = "HIGH";
                    } else {
                        intensity = "MEDIUM";
                    }

                    WorkoutRequest request = new WorkoutRequest(title, duration, calories, intensity, null);
                    api.createWorkout(request).enqueue(new Callback<Workout>() {
                        @Override
                        public void onResponse(Call<Workout> call, Response<Workout> response) {
                            if (isAdded()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Workout logged!", Toast.LENGTH_SHORT).show();
                                    loadWorkouts();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to add workout", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Workout> call, Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggle(Workout workout) {
        api.toggleWorkout(workout.getId()).enqueue(new Callback<Workout>() {
            @Override
            public void onResponse(Call<Workout> call, Response<Workout> response) {
                if (isAdded()) loadWorkouts();
            }

            @Override
            public void onFailure(Call<Workout> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDelete(Workout workout) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Remove \"" + workout.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    api.deleteWorkout(workout.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (isAdded()) loadWorkouts();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
