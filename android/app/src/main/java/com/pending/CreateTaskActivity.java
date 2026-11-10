package com.pending;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pending.model.PendingItem;
import com.pending.repository.NetworkCallback;
import com.pending.repository.PendingRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {

    private PendingRepository repository;
    private Long existingTaskId = null;
    private PendingItem existingTask;

    public static Intent newIntent(android.content.Context context, PendingItem task) {
        Intent intent = new Intent(context, CreateTaskActivity.class);
        if (task != null) {
            intent.putExtra("TASK_ID", task.getId().longValue());
            intent.putExtra("TASK_TITLE", task.getTitle());
            intent.putExtra("TASK_NOTES", task.getNotes());
            intent.putExtra("TASK_DATE", task.getExpectedResponseDate());
            intent.putExtra("TASK_PRIORITY", task.getPriority() == null ? 1 : task.getPriority());
            intent.putExtra("TASK_SNAPSHOT", new com.google.gson.Gson().toJson(task));
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        repository = new PendingRepository(this);

        EditText title = findViewById(R.id.taskTitleInput);
        EditText waitingOn = findViewById(R.id.taskWaitingOnInput);
        EditText date = findViewById(R.id.taskDateInput);
        EditText details = findViewById(R.id.taskDetailsInput);
        TextView reminderInput = findViewById(R.id.taskReminderInput);

        Spinner prioritySpinner = findViewById(R.id.spinnerPriority);
        String[] priorityOptions = new String[]{"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                priorityOptions
        );
        prioritySpinner.setAdapter(priorityAdapter);
        prioritySpinner.setSelection(1);

        Button save = findViewById(R.id.btnSaveTask);
        Button delete = findViewById(R.id.btnDeleteTask);
        Button cancel = findViewById(R.id.btnCancelTask);

        // Edit check
        Intent intent = getIntent();
        if (intent.hasExtra("TASK_ID")) {
            existingTaskId = intent.getLongExtra("TASK_ID", -1);
            existingTask = new com.google.gson.Gson().fromJson(
                    intent.getStringExtra("TASK_SNAPSHOT"), PendingItem.class);
            waitingOn.setText(existingTask == null
                    ? intent.getStringExtra("TASK_WAITING_ON") : existingTask.getWaitingOn());
            title.setText(intent.getStringExtra("TASK_TITLE"));
            details.setText(intent.getStringExtra("TASK_NOTES"));

            String existingDate = intent.getStringExtra("TASK_DATE");
            if (existingDate != null && !existingDate.isEmpty()) {
                date.setText(existingDate);
            }

            int existingPriority = intent.getIntExtra("TASK_PRIORITY", 1);
            if (existingPriority >= 0 && existingPriority <= 2) {
                prioritySpinner.setSelection(existingPriority);
            }

            if (intent.hasExtra("TASK_REMINDER") && reminderInput != null) {
                String existingReminder = intent.getStringExtra("TASK_REMINDER");
                if (existingReminder != null && !existingReminder.isEmpty()) {
                    reminderInput.setText(existingReminder);
                }
            }

            save.setText("Update Task");

            if (delete != null) {
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(v -> confirmDeleteTask());
            }
        }

        date.setFocusable(false);

        date.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    this,
                    (picker, year, month, day) -> {
                        new TimePickerDialog(
                                this,
                                (tp, hourOfDay, minute) -> {
                                    String formatted = String.format(
                                            Locale.US,
                                            "%04d-%02d-%02d %02d:%02d",
                                            year, month + 1, day, hourOfDay, minute
                                    );
                                    date.setText(formatted);
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                false
                        ).show();
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        if (reminderInput != null) {
            reminderInput.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(
                        this,
                        (picker, year, month, day) -> {
                            new TimePickerDialog(
                                    this,
                                    (tp, hourOfDay, minute) -> {
                                        String formatted = String.format(
                                                Locale.US,
                                                "%04d-%02d-%02d %02d:%02d",
                                                year, month + 1, day, hourOfDay, minute
                                        );
                                        reminderInput.setText(formatted);
                                    },
                                    c.get(Calendar.HOUR_OF_DAY),
                                    c.get(Calendar.MINUTE),
                                    false
                            ).show();
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                ).show();
            });
        }

        save.setOnClickListener(v -> {
            String t = title.getText().toString().trim();
            String d = date.getText().toString().trim();
            String n = details.getText().toString().trim();
            String waiting = waitingOn.getText().toString().trim();
            int selectedPriority = prioritySpinner.getSelectedItemPosition();

            if (t.isEmpty()) {
                title.setError("Enter a title");
                return;
            }

            if (d.isEmpty()) {
                date.setError("Choose a date and time");
                return;
            }

            String serverDate = d.contains(" ") ? d.split(" ")[0] : d;

            save.setEnabled(false);

            if (existingTaskId != null && existingTaskId != -1) {
                PendingItem toUpdate = existingTask == null ? new PendingItem()
                        : com.pending.ui.PendingTaskUpdates.copyForUpdate(existingTask);
                toUpdate.setId(existingTaskId);

                toUpdate.setTitle(t);
                toUpdate.setNotes(n);
                toUpdate.setWaitingOn(waiting);
                toUpdate.setExpectedResponseDate(serverDate);
                if (existingTask == null) toUpdate.setStatus(0);
                toUpdate.setPriority(selectedPriority);


                repository.updateItem(existingTaskId, toUpdate, new NetworkCallback<PendingItem>() {
                    @Override
                    public void onSuccess(PendingItem result) {
                        save.setEnabled(true);
                        Toast.makeText(CreateTaskActivity.this, "Task updated: " + toUpdate.getTitle(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        save.setEnabled(true);
                        Toast.makeText(CreateTaskActivity.this, "Failed to update: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                repository.createItem(t, waiting, startDate, serverDate, 0, n, selectedPriority, new NetworkCallback<PendingItem>() {
                    @Override
                    public void onSuccess(PendingItem result) {
                        save.setEnabled(true);
                        Toast.makeText(CreateTaskActivity.this, "Task created: " + t, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        save.setEnabled(true);
                        Toast.makeText(CreateTaskActivity.this, "Failed to save: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        cancel.setOnClickListener(v -> finish());
    }

    private void confirmDeleteTask() {
        if (existingTaskId == null || existingTaskId <= 0) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this pending task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteItem(existingTaskId, new NetworkCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            Toast.makeText(CreateTaskActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(CreateTaskActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}