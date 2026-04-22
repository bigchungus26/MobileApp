package com.example.smarttracker.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttracker.R;
import com.example.smarttracker.data.TaskView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskView> tasks = new ArrayList<>();
    private final OnTaskToggleListener listener;

    public interface OnTaskToggleListener {
        void onToggle(int taskId);
    }

    public TaskAdapter(OnTaskToggleListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskView> tasks) {
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
        TaskView task = tasks.get(position);
        String description = task.description == null || task.description.isEmpty()
                ? "Daily task" : task.description;

        holder.tvTitle.setText(task.title);
        holder.tvSubtitle.setText(description);

        holder.checkTask.setOnCheckedChangeListener(null);
        holder.checkTask.setChecked(task.completed);

        if (task.completed) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(task.id);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTask;
        TextView tvTitle, tvSubtitle;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = itemView.findViewById(R.id.checkTask);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
        }
    }
}
