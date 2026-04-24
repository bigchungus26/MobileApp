package com.example.smarttracker.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private JSONArray tasks = new JSONArray();
    private OnTaskToggleListener listener;

    public interface OnTaskToggleListener {
        void onToggle(int taskId);
    }

    public TaskAdapter(OnTaskToggleListener listener) {
        this.listener = listener;
    }

    public void setTasks(JSONArray tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        try {
            JSONObject task = tasks.getJSONObject(position);
            String title = task.getString("title");
            String description = task.optString("description", "Daily task");
            boolean completed = task.getInt("completed") == 1;
            final int taskId = task.getInt("id");

            holder.tvTitle.setText(title);
            if (description.isEmpty()) {
                holder.tvSubtitle.setText("Daily task");
            } else {
                holder.tvSubtitle.setText(description);
            }

            holder.checkTask.setOnCheckedChangeListener(null);
            holder.checkTask.setChecked(completed);

            if (completed) {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            holder.checkTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) listener.onToggle(taskId);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tasks.length();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTask;
        TextView tvTitle, tvSubtitle;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = (CheckBox) itemView.findViewById(R.id.checkTask);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = (TextView) itemView.findViewById(R.id.tvTaskSubtitle);
        }
    }
}
