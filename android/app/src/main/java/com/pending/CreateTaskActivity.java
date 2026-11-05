package com.pending;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    // Should stay null when creating a new task
    private Long existingTaskId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_task);

        repository = new PendingRepository(this);

        EditText title = findViewById(R.id.taskTitleInput);
        EditText date = findViewById(R.id.taskDateInput);
        EditText details = findViewById(R.id.taskDetailsInput);

        Button save = findViewById(R.id.btnSaveTask);
        Button cancel = findViewById(R.id.btnCancelTask);

        // Existing task check
        Intent intent = getIntent();
        if(intent.hasExtra("TASK_ID")) {
            existingTaskId = intent.getLongExtra("TASK_ID", -1);
            title.setText(intent.getStringExtra("TASK_TITLE"));
            details.setText(intent.getStringExtra("TASK_NOTES"));

            String existingDate = intent.getStringExtra("TASK_DATE");
            if(existingDate != null && !existingDate.isEmpty()) {
                date.setText(existingDate);
            }
            save.setText("Update Task");
        }

        date.setFocusable(false);

        date.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (picker, year, month, day) -> {
                        String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
                        date.setText(formattedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        save.setOnClickListener(v -> {

            String t = title.getText().toString().trim();
            String d = date.getText().toString().trim();
            String n = details.getText().toString().trim();

            if (t.isEmpty()) {
                title.setError("Enter a title");
                return;
            }

            if (d.isEmpty()) {
                date.setError("Choose a date");
                return;
            }

            // Updating existing task
            if (existingTaskId != null && existingTaskId != -1) {
                PendingItem toUpdate = new PendingItem();
                toUpdate.setId(existingTaskId);
                toUpdate.setTitle(t);
                toUpdate.setNotes(n);
                toUpdate.setExpectedResponseDate(d);
                // TODO: Missing waiting on
                toUpdate.setStatus(0);
                toUpdate.setPriority(1);

                repository.updateItem(existingTaskId, toUpdate, new NetworkCallback<PendingItem>() {
                    @Override
                    public void onSuccess(PendingItem result) {
                        Toast.makeText(CreateTaskActivity.this, "Task updated: " + toUpdate.getTitle(), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(CreateTaskActivity.this, "Failed to update: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                repository.createItem(t, null, startDate, d, 0, n, 1, new NetworkCallback<PendingItem>() {
                    @Override
                    public void onSuccess(PendingItem result) {
                        Toast.makeText(CreateTaskActivity.this, "Task created: " + t, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(CreateTaskActivity.this, "Failed to save: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        cancel.setOnClickListener(v -> finish());
    }
}
