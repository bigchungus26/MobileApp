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
import com.example.smarttracker.data.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    //the list of workouts currently shown
    private List<Workout> workouts = new ArrayList<>();
    //callback fired for both toggle and delete actions
    private final OnWorkoutActionListener listener;

    //interface so the activity can react to row actions
    public interface OnWorkoutActionListener {
        void onToggle(int workoutId);
        void onDelete(int workoutId, String title);
    }

    public WorkoutAdapter(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    //replace the dataset and tell the recycler to redraw
    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
        notifyDataSetChanged();
    }

    //inflate one row from the item_workout layout
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    //fill in one row with the workout data
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);

        holder.tvTitle.setText(workout.title);
        holder.tvDuration.setText(workout.durationMinutes + " min");
        holder.tvCalories.setText(workout.calories + " cal");
        holder.tvIntensity.setText(workout.intensity);

        //clear the listener before setting checked so we don't trigger it
        holder.checkWorkout.setOnCheckedChangeListener(null);
        holder.checkWorkout.setChecked(workout.completed);

        //strike through the title when the workout is done
        if (workout.completed) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //hand the toggle action back up to the activity
        holder.checkWorkout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(workout.id);
        });

        //hand the delete action back up to the activity
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(workout.id, workout.title);
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    //caches the views inside a single workout row
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
