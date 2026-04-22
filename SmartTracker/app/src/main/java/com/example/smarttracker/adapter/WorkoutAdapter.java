package com.example.smarttracker.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.model.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workouts = new ArrayList<>();
    private OnWorkoutActionListener listener;

    public interface OnWorkoutActionListener {
        void onToggle(Workout workout);
        void onDelete(Workout workout);
    }

    public WorkoutAdapter(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        holder.tvTitle.setText(workout.getTitle());
        holder.tvDuration.setText(workout.getDurationMinutes() + " min");
        holder.tvCalories.setText(workout.getCalories() + " cal");
        holder.tvIntensity.setText(workout.getIntensity());

        holder.checkWorkout.setOnCheckedChangeListener(null);
        holder.checkWorkout.setChecked(workout.isCompleted());

        if (workout.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.checkWorkout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(workout);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(workout);
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkWorkout;
        TextView tvTitle, tvDuration, tvCalories, tvIntensity;
        ImageView ivDelete;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            checkWorkout = itemView.findViewById(R.id.checkWorkout);
            tvTitle = itemView.findViewById(R.id.tvWorkoutTitle);
            tvDuration = itemView.findViewById(R.id.tvWorkoutDuration);
            tvCalories = itemView.findViewById(R.id.tvWorkoutCalories);
            tvIntensity = itemView.findViewById(R.id.tvWorkoutIntensity);
            ivDelete = itemView.findViewById(R.id.ivDeleteWorkout);
        }
    }
}
