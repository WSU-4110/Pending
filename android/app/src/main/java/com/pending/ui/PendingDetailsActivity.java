package com.pending.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.gson.Gson;
import com.pending.R;
import com.pending.model.PendingItem;
import com.pending.network.RetrofitClient;
import com.pending.repository.*;
import java.util.*;
import retrofit2.*;

/** Details for API tasks. The legacy local-task details activity remains unchanged. */
public class PendingDetailsActivity extends AppCompatActivity {
  private PendingItem task;
  private PendingRepository repository;
  private Call<PendingItem> load;
  private boolean saving;

  private TextView text(int id) {
    return findViewById(id);
  }

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    new PendingUiChrome(this);
    setContentView(R.layout.pending_ui_details);
    repository = new PendingRepository(this);
    View root = findViewById(R.id.detailsRoot);
    int padding = Math.round(24 * getResources().getDisplayMetrics().density);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          androidx.core.graphics.Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(padding + b.left, b.top + padding, padding + b.right, padding + b.bottom);
          return insets;
        });
    findViewById(R.id.detailsBack).setOnClickListener(v -> finish());
    findViewById(R.id.detailsEdit).setOnClickListener(v -> edit());
    findViewById(R.id.detailsComplete)
        .setOnClickListener(
            v ->
                new AlertDialog.Builder(this)
                    .setTitle("Complete task?")
                    .setMessage(task.getTitle())
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(
                        "Complete",
                        (d, w) -> {
                          PendingItem copy = copy();
                          copy.setStatus(3);
                          save(copy, null);
                        })
                    .show());
    ((PendingProgressRingView) findViewById(R.id.detailsRing)).setUnavailable();
    enable(false);
    fetch();
  }

  private void enable(boolean enabled) {
    findViewById(R.id.detailsEdit).setEnabled(enabled);
    findViewById(R.id.detailsComplete)
        .setEnabled(enabled && task != null && !PendingTaskPresentation.completed(task));
  }

  private void fetch() {
    long id = getIntent().getLongExtra("task_id", -1);
    if (id < 0) {
      error("Task not found");
      return;
    }
    load = RetrofitClient.getInstance(this).getItemById(id);
    load.enqueue(
        new Callback<PendingItem>() {
          public void onResponse(Call<PendingItem> c, Response<PendingItem> r) {
            if (isFinishing() || isDestroyed()) return;
            if (r.isSuccessful() && r.body() != null && PendingTaskPresentation.active(r.body())) {
              task = r.body();
              render();
            } else error("Task unavailable. Tap to retry.");
          }

          public void onFailure(Call<PendingItem> c, Throwable t) {
            if (!c.isCanceled() && !isFinishing() && !isDestroyed())
              error("Couldn’t load task. Tap to retry.");
          }
        });
  }

  private void error(String message) {
    text(R.id.detailsState).setVisibility(View.VISIBLE);
    text(R.id.detailsState).setText(message);
    text(R.id.detailsState).setOnClickListener(v -> fetch());
    enable(false);
  }

  private void render() {
    text(R.id.detailsState).setVisibility(View.GONE);
    text(R.id.detailsTitle).setText(task.getTitle());
    text(R.id.detailsPriority)
        .setText(
            task.getPriority() == null ? "Priority unavailable" : "Priority " + task.getPriority());
    text(R.id.detailsStatus)
        .setText(PendingTaskPresentation.completed(task) ? "Completed" : "Open");
    text(R.id.detailsDue)
        .setText(
            PendingTaskPresentation.due(task).isEmpty()
                ? "No due date"
                : displayDueDate());
    text(R.id.detailsNotes)
        .setText(
            task.getNotes() == null || task.getNotes().trim().isEmpty()
                ? "No notes yet"
                : task.getNotes());
    text(R.id.detailsComplete)
        .setText(PendingTaskPresentation.completed(task) ? "✓  Completed" : "✓  Mark as completed");
    enable(!saving);
  }

  private String displayDueDate() {
    String value = PendingTaskPresentation.due(task);
    try {
      java.text.SimpleDateFormat source = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
      source.setLenient(false);
      return new java.text.SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(source.parse(value));
    } catch (java.text.ParseException exception) { return "No due date"; }
  }

  private PendingItem copy() {
    return PendingTaskUpdates.copyForUpdate(task);
  }

  private void save(PendingItem update, AlertDialog dialog) {
    if (saving) return;
    saving = true;
    enable(false);
    if (dialog != null) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    repository.updateItem(
        task.getId(),
        update,
        new NetworkCallback<PendingItem>() {
          public void onSuccess(PendingItem result) {
            saving = false;
            if (isFinishing() || isDestroyed()) return;
            task = result;
            if (dialog != null) dialog.dismiss();
            render();
          }

          public void onError(String error) {
            saving = false;
            if (isFinishing() || isDestroyed()) return;
            enable(true);
            if (dialog != null) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            Toast.makeText(
                    PendingDetailsActivity.this, "Changes not saved: " + error, Toast.LENGTH_LONG)
                .show();
          }
        });
  }

  private EditText field(LinearLayout box, String hint, String value) {
    EditText e = new EditText(this);
    e.setHint(hint);
    e.setText(value);
    box.addView(e);
    return e;
  }

  private void edit() {
    if (task == null || saving) return;
    LinearLayout box = new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL);
    int p = (int) (20 * getResources().getDisplayMetrics().density);
    box.setPadding(p, p, p, p);
    EditText title = field(box, "Title", task.getTitle()),
        waiting = field(box, "Waiting on", task.getWaitingOn()),
        notes = field(box, "Notes", task.getNotes());
    Button due = new Button(this);
    due.setAllCaps(false);
    final String[] date = {task.getExpectedResponseDate()};
    due.setText(date[0] == null ? "Choose due date" : date[0]);
    box.addView(due);
    due.setOnClickListener(
        v -> {
          Calendar c = Calendar.getInstance();
          String current = PendingTaskPresentation.due(task);
          if (!current.isEmpty()) {
            String[] parts = current.split("-");
            c.set(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]) - 1,
                Integer.parseInt(parts[2]));
          }
          new android.app.DatePickerDialog(
                  this,
                  (picker, y, m, d) -> {
                    date[0] = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    due.setText(date[0]);
                  },
                  c.get(Calendar.YEAR),
                  c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH))
              .show();
        });
    ScrollView scroll = new ScrollView(this);
    scroll.addView(box);
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle("Edit task")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                    v -> {
                      if (title.getText().toString().trim().isEmpty()) {
                        title.setError("Enter a title");
                        return;
                      }
                      PendingItem update = copy();
                      update.setTitle(title.getText().toString().trim());
                      update.setWaitingOn(waiting.getText().toString().trim());
                      update.setNotes(notes.getText().toString().trim());
                      update.setExpectedResponseDate(date[0]);
                      save(update, dialog);
                    }));
    dialog.show();
  }

  @Override
  protected void onDestroy() {
    if (load != null) load.cancel();
    super.onDestroy();
  }
}
