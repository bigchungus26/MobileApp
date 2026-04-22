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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private JSONArray workouts = new JSONArray();
    private OnWorkoutActionListener listener;

    public interface OnWorkoutActionListener {
        void onToggle(int workoutId);
        void onDelete(int workoutId, String title);
    }

    public WorkoutAdapter(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    public void setWorkouts(JSONArray workouts) {
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
        try {
            JSONObject workout = workouts.getJSONObject(position);
            String title = workout.getString("title");
            int duration = workout.optInt("duration_minutes", 0);
            int calories = workout.optInt("calories", 0);
            String intensity = workout.optString("intensity", "MEDIUM");
            boolean completed = workout.getInt("completed") == 1;
            int workoutId = workout.getInt("id");

            holder.tvTitle.setText(title);
            holder.tvDuration.setText(duration + " min");
            holder.tvCalories.setText(calories + " cal");
            holder.tvIntensity.setText(intensity);

            holder.checkWorkout.setOnCheckedChangeListener(null);
            holder.checkWorkout.setChecked(completed);

            if (completed) {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            holder.checkWorkout.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onToggle(workoutId);
            });

            holder.ivDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(workoutId, title);
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return workouts.length();
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
