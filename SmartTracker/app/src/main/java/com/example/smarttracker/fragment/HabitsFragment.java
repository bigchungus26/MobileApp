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
import com.example.smarttracker.data.Habit;
import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HabitsFragment extends Fragment implements HabitAdapter.OnHabitDeleteListener {

    private RecyclerView recyclerHabits;
    private HabitAdapter habitAdapter;
    private ProgressBar progressHabits;
    private TextView tvEmpty;
    private Repository repository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = Repository.get(requireContext());
        sessionManager = new SessionManager(requireContext());

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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadHabits();
    }

    private void loadHabits() {
        progressHabits.setVisibility(View.GONE);
        List<Habit> habits = repository.getHabits(sessionManager.getUserId());
        habitAdapter.setHabits(habits);
        tvEmpty.setVisibility(habits.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerHabits.setVisibility(habits.isEmpty() ? View.GONE : View.VISIBLE);
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

                    repository.addHabit(sessionManager.getUserId(), title, description,
                            category, frequency);
                    Toast.makeText(requireContext(), "Habit created!", Toast.LENGTH_SHORT).show();
                    loadHabits();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(int habitId, String title) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteHabit(habitId, sessionManager.getUserId());
                    loadHabits();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
