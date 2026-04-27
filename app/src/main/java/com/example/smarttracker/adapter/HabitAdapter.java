package com.example.smarttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.data.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    //the data the adapter is currently displaying
    private List<Habit> habits = new ArrayList<>();
    //callback fired when the trash icon is tapped
    private final OnHabitDeleteListener listener;

    //interface the activity implements so the adapter can ask for a delete
    public interface OnHabitDeleteListener {
        void onDelete(int habitId, String title);
    }

    public HabitAdapter(OnHabitDeleteListener listener) {
        this.listener = listener;
    }

    //replace the dataset and tell the recycler to redraw
    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    //build a single row from the item_habit layout
    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    //fill in one row with the data for the habit at this position
    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        //fall back to the category if no description was given
        String description;
        if (habit.description != null && !habit.description.isEmpty()) {
            description = habit.description;
        } else if (habit.category != null && !habit.category.isEmpty()) {
            description = habit.category;
        } else {
            description = "No description";
        }

        holder.tvTitle.setText(habit.title);
        holder.tvDescription.setText(description);
        holder.tvFrequency.setText(habit.frequency);
        holder.tvStreak.setText("Streak: " + habit.streak);

        //pass the delete tap back to the activity
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(habit.id, habit.title);
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    //holds the views for a single habit row so we don't keep re-finding them
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
