package com.example.smarttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private JSONArray habits = new JSONArray();
    private OnHabitDeleteListener listener;

    public interface OnHabitDeleteListener {
        void onDelete(int habitId, String title);
    }

    public HabitAdapter(OnHabitDeleteListener listener) {
        this.listener = listener;
    }

    public void setHabits(JSONArray habits) {
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
        try {
            JSONObject habit = habits.getJSONObject(position);
            final String title = habit.getString("title");
            String description = habit.optString("description", "");
            String category = habit.optString("category", "");
            String frequency = habit.optString("frequency", "DAILY");
            int streak = habit.optInt("streak", 0);
            final int habitId = habit.getInt("id");

            holder.tvTitle.setText(title);
            if (!description.isEmpty()) {
                holder.tvDescription.setText(description);
            } else if (!category.isEmpty()) {
                holder.tvDescription.setText(category);
            } else {
                holder.tvDescription.setText("No description");
            }
            holder.tvFrequency.setText(frequency);
            holder.tvStreak.setText("Streak: " + streak);

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onDelete(habitId, title);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return habits.length();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvFrequency, tvStreak;
        ImageView ivDelete;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvHabitTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvHabitDescription);
            tvFrequency = (TextView) itemView.findViewById(R.id.tvHabitFrequency);
            tvStreak = (TextView) itemView.findViewById(R.id.tvHabitStreak);
            ivDelete = (ImageView) itemView.findViewById(R.id.ivDeleteHabit);
        }
    }
}
