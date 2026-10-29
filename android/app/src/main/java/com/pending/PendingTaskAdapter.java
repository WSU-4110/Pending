
package com.pending;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pending.model.PendingItem;

import java.util.ArrayList;
import java.util.List;

public class PendingTaskAdapter
        extends RecyclerView.Adapter<PendingTaskAdapter.TaskViewHolder> {

    private final List<PendingItem> tasks = new ArrayList<>();
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(PendingItem item);
    }

    public PendingTaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<PendingItem> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_pending_task, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder, int position) {

        PendingItem item = tasks.get(position);

        holder.title.setText(item.getTitle());

        String description = item.getNotes();

        if (description == null || description.isEmpty()) {
            description = item.getWaitingOn();
        }

        holder.description.setText(
                description != null ? description : ""
        );

        holder.date.setText(
                item.getExpectedResponseDate() != null
                        ? item.getExpectedResponseDate()
                        : "No date"
        );

        holder.itemView.setOnClickListener(
                v -> listener.onTaskClick(item)
        );

        holder.viewButton.setOnClickListener(
                v -> listener.onTaskClick(item)
        );
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        TextView date;
        View viewButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.taskName);
            description = itemView.findViewById(R.id.taskDescrition);
            date = itemView.findViewById(R.id.taskDate);
            viewButton = itemView.findViewById(R.id.viewTaskButton);
        }
    }
}
