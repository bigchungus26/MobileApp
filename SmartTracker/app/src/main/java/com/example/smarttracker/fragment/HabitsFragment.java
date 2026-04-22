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
import com.example.smarttracker.adapter.HabitAdapter;
import com.example.smarttracker.api.ApiClient;
import com.example.smarttracker.api.ApiService;
import com.example.smarttracker.model.Habit;
import com.example.smarttracker.model.HabitRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HabitsFragment extends Fragment implements HabitAdapter.OnHabitDeleteListener {

    private RecyclerView recyclerHabits;
    private HabitAdapter habitAdapter;
    private ProgressBar progressHabits;
    private TextView tvEmpty;
    private ApiService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = ApiClient.getApiService(requireContext());

        recyclerHabits = view.findViewById(R.id.recyclerHabits);
        progressHabits = view.findViewById(R.id.progressHabits);
        tvEmpty = view.findViewById(R.id.tvEmptyHabits);

        habitAdapter = new HabitAdapter(this);
        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHabits.setAdapter(habitAdapter);

        loadHabits();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
    }

    private void loadHabits() {
        progressHabits.setVisibility(View.VISIBLE);
        api.getActiveHabits().enqueue(new Callback<List<Habit>>() {
            @Override
            public void onResponse(Call<List<Habit>> call, Response<List<Habit>> response) {
                if (!isAdded()) return;
                progressHabits.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Habit> habits = response.body();
                    habitAdapter.setHabits(habits);
                    tvEmpty.setVisibility(habits.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerHabits.setVisibility(habits.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Habit>> call, Throwable t) {
                if (isAdded()) {
                    progressHabits.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load habits", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showAddHabitDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_habit, null);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etHabitTitle);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etHabitDescription);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etHabitCategory);
        RadioGroup rgFrequency = dialogView.findViewById(R.id.rgFrequency);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Habit")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description = etDescription.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String frequency = rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly
                            ? "WEEKLY" : "DAILY";

                    HabitRequest request = new HabitRequest(title, description, category, frequency);
                    api.createHabit(request).enqueue(new Callback<Habit>() {
                        @Override
                        public void onResponse(Call<Habit> call, Response<Habit> response) {
                            if (isAdded()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Habit created!", Toast.LENGTH_SHORT).show();
                                    loadHabits();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to create habit", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Habit> call, Throwable t) {
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
    public void onDelete(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + habit.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    api.deleteHabit(habit.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (isAdded()) loadHabits();
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
