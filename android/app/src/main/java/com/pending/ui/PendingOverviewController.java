package com.pending.ui;

import android.app.Activity;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.*;
import com.pending.R;
import com.pending.model.PendingItem;
import com.pending.repository.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class PendingOverviewController {
  public interface Host {
    void overview();

    void home();

    void completed();

    void settings();

    void create();

    void edit(PendingItem t);
  }

  private final Activity activity;
  private final PendingUiChrome chrome;
  private final PendingRepository repo;
  private final Host host;
  private final boolean overview;
  private final View root;
  private final PendingUiTaskAdapter adapter;
  private List<PendingItem> tasks = new ArrayList<>();
  private String query = "", day = null;
  private int state = 0, request = 0;
  private boolean disposed = false, loaded = false;

  public PendingOverviewController(
      Activity a, PendingRepository r, Host h, boolean overview, String name) {
    activity = a;
    chrome = new PendingUiChrome(a);
    repo = r;
    host = h;
    this.overview = overview;
    a.setContentView(R.layout.pending_ui_screen);
    root = a.findViewById(R.id.pendingUiRoot);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          androidx.core.graphics.Insets bars =
              insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return insets;
        });
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
    text(R.id.pendingUiGreeting)
        .setText(greeting + (name == null || name.trim().isEmpty() ? "" : ", " + name.trim()));
    if (overview)
      text(R.id.pendingUiSubtitle)
          .setText(new SimpleDateFormat("'Today,' MMM d", Locale.getDefault()).format(new Date()));
    if (!overview) {
      View bell = root.findViewById(R.id.pendingUiBell);
      ((android.view.ViewGroup) bell.getParent()).removeView(bell);
      LinearLayout brand = root.findViewById(R.id.pendingUiBrand);
      TextView brandName = root.findViewById(R.id.pendingUiBrandName);
      LinearLayout.LayoutParams nameParams =
          (LinearLayout.LayoutParams) brandName.getLayoutParams();
      nameParams.width = 0;
      nameParams.weight = 1;
      brandName.setLayoutParams(nameParams);
      brand.addView(bell);
    }
    visible(R.id.pendingUiSearch, !overview);
    visible(R.id.pendingUiBrand, !overview);
    visible(R.id.pendingUiStats, !overview);
    visible(R.id.pendingUiProgressSection, !overview);
    visible(R.id.pendingUiWeek, overview);
    visible(R.id.pendingUiFiltersScroll, overview);
    visible(R.id.pendingUiCategories, overview);
    root.findViewById(R.id.pendingUiBell)
        .setOnClickListener(
            v ->
                Toast.makeText(a, "Notifications are not available yet", Toast.LENGTH_SHORT)
                    .show());
    root.findViewById(R.id.pendingUiNavPending).setOnClickListener(v -> h.home());
    root.findViewById(R.id.pendingUiNavCompleted)
        .setOnClickListener(
            v -> {
              dispose();
              h.completed();
            });
    root.findViewById(R.id.pendingUiNavSettings)
        .setOnClickListener(
            v -> {
              dispose();
              h.settings();
            });
    root.findViewById(R.id.pendingUiOpen).setOnClickListener(v -> h.overview());
    root.findViewById(R.id.pendingUiDone)
        .setOnClickListener(
            v -> {
              dispose();
              h.completed();
            });
    root.findViewById(R.id.pendingUiAdd).setOnClickListener(v -> h.create());
    text(R.id.pendingUiListTitle).setText(overview ? "All tasks" : "Today’s tasks");
    text(R.id.pendingUiSeeAll).setText(overview ? "Clear filters" : "See all");
    text(R.id.pendingUiSeeAll)
        .setOnClickListener(
            v -> {
              if (!overview) h.overview();
              else {
                day = null;
                state = 0;
                ((EditText) root.findViewById(R.id.pendingUiSearch)).setText("");
                render();
              }
            });
    adapter =
        new PendingUiTaskAdapter(
            overview,
            new PendingUiTaskAdapter.Actions() {
              public void open(PendingItem t) {
                activity.startActivity(
                    new android.content.Intent(activity, PendingDetailsActivity.class)
                        .putExtra("task_id", t.getId().longValue()));
              }

              public void edit(PendingItem t) {
                h.edit(t);
              }

              public void complete(PendingItem t) {
                markCompleted(t);
              }
            });
    RecyclerView list = root.findViewById(R.id.pendingUiList);
    list.setLayoutManager(new LinearLayoutManager(a));
    list.setAdapter(adapter);
    ((EditText) root.findViewById(R.id.pendingUiSearch))
        .addTextChangedListener(
            new TextWatcher() {
              public void beforeTextChanged(CharSequence s, int st, int count, int after) {}

              public void onTextChanged(CharSequence s, int st, int before, int count) {
                query = s.toString();
                if (loaded) render();
              }

              public void afterTextChanged(Editable e) {}
            });
    refresh();
  }

  private TextView text(int id) {
    return root.findViewById(id);
  }

  private void visible(int id, boolean show) {
    root.findViewById(id).setVisibility(show ? View.VISIBLE : View.GONE);
  }

  public void dispose() {
    if (disposed) return;
    chrome.restore();
    disposed = true;
    request++;
  }

  public void refresh() {
    if (disposed) return;
    int token = ++request;
    text(R.id.pendingUiState).setText("Loading tasks…");
    visible(R.id.pendingUiState, true);
    text(R.id.pendingUiState).setOnClickListener(null);
    repo.fetchAllItems(
        new NetworkCallback<List<PendingItem>>() {
          public void onSuccess(List<PendingItem> items) {
            if (disposed || token != request) return;
            tasks = items;
            loaded = true;
            render();
          }

          public void onError(String error) {
            if (disposed || token != request) return;
            text(R.id.pendingUiState).setText("Couldn’t refresh tasks. Tap to retry.");
            text(R.id.pendingUiState).setOnClickListener(v -> refresh());
          }
        });
  }

  private void render() {
    if (disposed) return;
    List<PendingItem> all = PendingTaskPresentation.filter(tasks, "", null, 0);
    int done = PendingTaskPresentation.filter(all, "", null, 2).size();
    text(R.id.pendingUiOpenCount).setText(String.valueOf(all.size() - done));
    text(R.id.pendingUiDoneCount).setText(String.valueOf(done));
    int[] week = PendingTaskPresentation.weekly(all, new Date());
    int percent = week[1] == 0 ? 0 : Math.round(100f * week[0] / week[1]);
    ((PendingProgressRingView) root.findViewById(R.id.pendingUiRing)).setProgress(percent);
    ((ProgressBar) root.findViewById(R.id.pendingUiBar)).setProgress(percent);
    text(R.id.pendingUiWeekProgress).setText(week[0] + " of " + week[1] + " tasks completed");
    String selected =
        overview ? day : (query.trim().isEmpty() ? PendingTaskPresentation.date(new Date()) : null);
    List<PendingItem> shown =
        PendingTaskPresentation.filter(all, query, selected, overview ? state : 1);
    adapter.submit(shown);
    visible(R.id.pendingUiState, shown.isEmpty());
    text(R.id.pendingUiState).setText("No tasks to show");
    text(R.id.pendingUiState).setOnClickListener(null);
    if (overview) {
      text(R.id.pendingUiListTitle).setText(day == null ? "All tasks" : "Tasks · " + day);
      renderWeek();
      renderFilters(all);
    } else
      text(R.id.pendingUiListTitle)
          .setText(query.trim().isEmpty() ? "Today’s tasks" : "Search results");
  }

  private TextView chip(String label, boolean selected) {
    TextView v = new TextView(activity);
    v.setText(label);
    v.setTextColor(0xFF234F37);
    v.setTextSize(14);
    v.setGravity(Gravity.CENTER);
    v.setPadding(dp(12), dp(8), dp(12), dp(8));
    v.setMinHeight(dp(48));
    v.setBackgroundResource(
        selected ? R.drawable.pending_ui_badge_sage : R.drawable.pending_ui_white);
    v.setSelected(selected);
    return v;
  }

  private int dp(int n) {
    return Math.round(n * activity.getResources().getDisplayMetrics().density);
  }

  private void renderFilters(List<PendingItem> all) {
    LinearLayout box = root.findViewById(R.id.pendingUiFilters);
    box.removeAllViews();
    String[] names = {"All", "Open", "Completed"};
    for (int i = 0; i < 3; i++) {
      final int index = i;
      TextView v =
          chip(names[i] + " " + PendingTaskPresentation.filter(all, "", day, i).size(), state == i);
      LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
      p.setMargins(0, 0, dp(8), 0);
      box.addView(v, p);
      v.setOnClickListener(
          x -> {
            state = index;
            render();
          });
    }
  }

  private void renderWeek() {
    LinearLayout box = root.findViewById(R.id.pendingUiWeek);
    box.removeAllViews();
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DATE, -(c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7);
    for (int i = 0; i < 7; i++) {
      String date = PendingTaskPresentation.date(c.getTime());
      LinearLayout column = new LinearLayout(activity);
      column.setOrientation(LinearLayout.VERTICAL);
      column.setGravity(Gravity.CENTER);
      TextView label = new TextView(activity);
      label.setText(new SimpleDateFormat("EEE", Locale.getDefault()).format(c.getTime()));
      label.setTextColor(0xFF545C56);
      label.setTextSize(13);
      label.setGravity(Gravity.CENTER);
      column.addView(label, new LinearLayout.LayoutParams(-1, dp(25)));
      TextView number = new TextView(activity);
      number.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
      number.setTextSize(16);
      number.setTypeface(null, android.graphics.Typeface.BOLD);
      number.setGravity(Gravity.CENTER);
      boolean selected = date.equals(day);
      number.setTextColor(selected ? 0xFFFFFFFF : 0xFF14291D);
      number.setBackgroundResource(
          selected ? R.drawable.pending_ui_day_selected : R.drawable.pending_ui_day);
      column.addView(number, new LinearLayout.LayoutParams(dp(40), dp(40)));
      box.addView(column, new LinearLayout.LayoutParams(0, -1, 1));
      column.setContentDescription(date);
      column.setSelected(selected);
      column.setOnClickListener(
          x -> {
            day = date.equals(day) ? null : date;
            render();
          });
      c.add(Calendar.DATE, 1);
    }
  }

  private void markCompleted(PendingItem t) {
    new androidx.appcompat.app.AlertDialog.Builder(activity)
        .setTitle("Complete task?")
        .setMessage(t.getTitle())
        .setNegativeButton("Cancel", null)
        .setPositiveButton(
            "Complete",
            (d, w) -> {
              PendingItem update = PendingTaskUpdates.copyForUpdate(t);
              update.setStatus(3);
              repo.updateItem(
                  t.getId(),
                  update,
                  new NetworkCallback<PendingItem>() {
                    public void onSuccess(PendingItem result) {
                      refresh();
                    }

                    public void onError(String error) {
                      if (!disposed)
                        Toast.makeText(
                                activity, "Task was not completed: " + error, Toast.LENGTH_LONG)
                            .show();
                    }
                  });
            })
        .show();
  }
}
