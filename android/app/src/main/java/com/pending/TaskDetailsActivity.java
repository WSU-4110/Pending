package com.pending;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TaskDetailsActivity extends AppCompatActivity {

    private LocalTaskStore.Task task;
    private LinearLayout body;

    private final int green = 0xFF2B806B;
    private final int ink = 0xFF203D35;

    private int dp(int n) {
        return (int) (
                n * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        long taskId = getIntent().getLongExtra("task_id", -1);

        task = LocalTaskStore.get(this, taskId);

        if (task == null) {
            finish();
            return;
        }

        showDetails();
    }

    private void base() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFF7FAF8);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        body.setPadding(
                dp(24), dp(32), dp(24), dp(24)
        );

        scroll.addView(body);
        setContentView(scroll);
    }

    private void label(String value, int size) {
        TextView t = new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(ink);

        t.setPadding(0, dp(8), 0, dp(8));

        body.addView(t);
    }

    private void action(String title, Runnable callback) {
        Button b = new Button(this);

        b.setText(title);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);

        b.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(green)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(52));

        p.topMargin = dp(10);

        body.addView(b, p);

        b.setOnClickListener(v -> callback.run());
    }

    private EditText edit(String value, String hint) {
        EditText e = new EditText(this);

        e.setHint(hint);
        e.setText(value);
        e.setTextColor(ink);

        body.addView(e);

        return e;
    }

    private void showDetails() {
        base();

        label("Task Details", 30);
        label(task.title, 24);

        label(
                "Expected response: " + task.date,
                16
        );

        label(
                "Details: " +
                        (task.notes.isEmpty()
                                ? "No details"
                                : task.notes),
                16
        );

        label(
                "Status: " +
                        (task.completed
                                ? "Completed"
                                : "Pending"),
                16
        );

        action(
                task.completed
                        ? "Mark as Pending"
                        : "Mark as Complete",
                () -> {
                    task.completed = !task.completed;

                    LocalTaskStore.save(this, task);

                    showDetails();
                }
        );

        action("Edit Task", this::showEdit);

        action("Delete Task", () -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete this task?")
                    .setMessage("This cannot be undone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {
                                LocalTaskStore.delete(
                                        this,
                                        task.id
                                );

                                finish();
                            }
                    )
                    .show();
        });

        action("Back to Dashboard", this::finish);
    }

    private void showEdit() {
        base();

        label("Edit Pending Task", 30);

        EditText title = edit(task.title, "Title");

        EditText date = edit(
                task.date,
                "Expected response date"
        );

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

        EditText notes = edit(task.notes, "Details");
        notes.setSingleLine(false);

        action("Save Changes", () -> {

            String newTitle =
                    title.getText().toString().trim();

            String newDate =
                    date.getText().toString().trim();

            if (newTitle.isEmpty()) {
                title.setError("Enter a title");
                return;
            }

            if (newDate.isEmpty()) {
                date.setError("Choose a date");
                return;
            }

            task.title = newTitle;
            task.date = newDate;
            task.notes =
                    notes.getText().toString().trim();

            LocalTaskStore.save(this, task);

            Toast.makeText(
                    this,
                    "Changes saved",
                    Toast.LENGTH_SHORT
            ).show();

            showDetails();
        });

        action("Cancel", this::showDetails);
    }
}
