package com.example.smarttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private OnHabitDeleteListener listener;

    public interface OnHabitDeleteListener {
        void onDelete(Habit habit);
    }

    public HabitAdapter(OnHabitDeleteListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvTitle.setText(habit.getTitle());
        holder.tvDescription.setText(
                habit.getDescription() != null && !habit.getDescription().isEmpty()
                        ? habit.getDescription()
                        : habit.getCategory() != null ? habit.getCategory() : "No description");
        holder.tvFrequency.setText(habit.getFrequency());
        holder.tvStreak.setText("Streak: " + habit.getStreak());

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(habit);
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvFrequency, tvStreak;
        ImageView ivDelete;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHabitTitle);
            tvDescription = itemView.findViewById(R.id.tvHabitDescription);
            tvFrequency = itemView.findViewById(R.id.tvHabitFrequency);
            tvStreak = itemView.findViewById(R.id.tvHabitStreak);
            ivDelete = itemView.findViewById(R.id.ivDeleteHabit);
        }
    }
}
