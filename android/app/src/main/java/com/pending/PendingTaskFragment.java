package com.pending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pending.model.PendingItem;

public class PendingTaskFragment extends Fragment {

    private TextView taskNameText;
    private TextView taskDescriptionText;
    private TextView taskDateText;
    private Button viewTaskButton;
    private PendingItem currentItem;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(PendingItem item);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskNameText = view.findViewById(R.id.taskName);
        taskDescriptionText = view.findViewById(R.id.taskDescrition);
        taskDateText = view.findViewById(R.id.taskDate);
        viewTaskButton = view.findViewById(R.id.viewTaskButton);

        if (viewTaskButton != null) {
            viewTaskButton.setOnClickListener(v -> {
                if (listener != null && currentItem != null) {
                    listener.onTaskClick(currentItem);
                }
            });
        }

        if (currentItem != null) {
            displayItem(currentItem);
        }
    }

    public void populateTask(PendingItem item, OnTaskClickListener clickListener) {
        this.currentItem = item;
        this.listener = clickListener;
        if (getView() != null) {
            displayItem(item);
        }
    }

    private void displayItem(PendingItem item) {
        if (taskNameText != null) {
            taskNameText.setText(item.getTitle() != null ? item.getTitle() : "Untitled Task");
        }
        if (taskDescriptionText != null) {
            String desc = item.getWaitingOn() != null && !item.getWaitingOn().isEmpty()
                    ? "Waiting on: " + item.getWaitingOn()
                    : item.getNotes();
            taskDescriptionText.setText(desc != null ? desc : "");
        }
        if (taskDateText != null) {
            taskDateText.setText(item.getExpectedResponseDate() != null ? item.getExpectedResponseDate() : "");
        }
    }
}