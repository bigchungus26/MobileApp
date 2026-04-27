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
import com.example.smarttracker.data.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    //the list of tasks currently shown
    private List<Task> tasks = new ArrayList<>();
    //callback fired when a checkbox is toggled
    private final OnTaskToggleListener listener;

    //interface so the activity can react to a task being checked
    public interface OnTaskToggleListener {
        void onToggle(int taskId);
    }

    public TaskAdapter(OnTaskToggleListener listener) {
        this.listener = listener;
    }

    //replace the tasks and tell the recycler to redraw
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    //inflate one row from the item_task layout
    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskHolder(view);
    }

    //fill in one row with the task data
    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task task = tasks.get(position);
        //use a default subtitle when no description is available
        String description = task.description == null || task.description.isEmpty()
                ? "Daily task" : task.description;

        holder.tvTitle.setText(task.title);
        holder.tvSubtitle.setText(description);

        //clear the listener before setting the checked state so we don't fire it accidentally
        holder.checkTask.setOnCheckedChangeListener(null);
        holder.checkTask.setChecked(task.completed);

        //draw a strike-through line when the task is done
        if (task.completed) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //attach the real listener that calls back into the activity
        holder.checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(task.id);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    //caches the views inside a single task row
    static class TaskHolder extends RecyclerView.ViewHolder {
        CheckBox checkTask;
        TextView tvTitle, tvSubtitle;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = itemView.findViewById(R.id.checkTask);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvSubtitle = itemView.findViewById(R.id.tvTaskSubtitle);
        }
    }
}
