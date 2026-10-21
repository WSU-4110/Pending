package com.pending;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_task);

        EditText title = findViewById(R.id.taskTitleInput);
        EditText date = findViewById(R.id.taskDateInput);
        EditText details = findViewById(R.id.taskDetailsInput);

        Button save = findViewById(R.id.btnSaveTask);
        Button cancel = findViewById(R.id.btnCancelTask);

        date.setFocusable(false);

        date.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            new DatePickerDialog(
                    this,
                    (picker, year, month, day) -> {
                        date.setText(
                                (month + 1) + "/" +
                                        day + "/" + year
                        );
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

            LocalTaskStore.Task task =
                    new LocalTaskStore.Task(
                            System.currentTimeMillis(),
                            t,
                            d,
                            n,
                            false
                    );

            LocalTaskStore.save(this, task);

            Toast.makeText(
                    this,
                    "Task saved on this device",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        });

        cancel.setOnClickListener(v -> finish());
    }
}
