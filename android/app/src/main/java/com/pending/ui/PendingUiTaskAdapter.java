package com.pending.ui;

import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.pending.R;
import com.pending.model.PendingItem;
import java.util.*;

public class PendingUiTaskAdapter extends RecyclerView.Adapter<PendingUiTaskAdapter.Holder> {
  public interface Actions {
    void edit(PendingItem t);


    void complete(PendingItem t);
  }

  private List<PendingItem> tasks = new ArrayList<>();
  private final boolean overview;
  private final Actions actions;

  public PendingUiTaskAdapter(boolean overview, Actions actions) {
    this.overview = overview;
    this.actions = actions;
  }

  public void submit(List<PendingItem> list) {
    tasks = new ArrayList<>(list);
    notifyDataSetChanged();
  }

  @Override
  public Holder onCreateViewHolder(ViewGroup p, int type) {
    return new Holder(
        LayoutInflater.from(p.getContext()).inflate(R.layout.pending_ui_task_row, p, false));
  }

  @Override
  public int getItemCount() {
    return tasks.size();
  }

  @Override
  public void onBindViewHolder(Holder h, int i) {
    PendingItem t = tasks.get(i);
    h.title.setText(t.getTitle());
    String notes = t.getNotes();
    if (notes == null || notes.isEmpty()) notes = t.getWaitingOn();
    boolean complete = PendingTaskPresentation.completed(t);
    String priority = t.getPriority() == null ? "Unspecified priority"
        : t.getPriority() == 2 ? "High priority" : t.getPriority() == 1 ? "Medium priority" : "Low priority";
    h.meta.setText(t.getCategory().name() + "  ·  " + priority);
    h.notes.setText(notes);
    h.notes.setVisibility(overview || notes == null || notes.isEmpty() ? View.GONE : View.VISIBLE);
    h.due.setText(PendingTaskPresentation.dueLabel(t, new Date()));
    h.due.setBackgroundResource(
        PendingTaskPresentation.due(t).compareTo(PendingTaskPresentation.date(new Date())) <= 0
            ? R.drawable.pending_ui_badge_pink
            : R.drawable.pending_ui_badge_sage);
    if (complete) {
      String resolved = PendingTaskPresentation.completionDate(t);
      h.due.setText(resolved.isEmpty() ? "Completed" : "Completed · " + resolved);
      h.due.setBackgroundResource(R.drawable.pending_ui_badge_sage);
    }
    boolean completedPage = actions == null;
    h.itemView.setBackgroundResource(completedPage
        ? (i % 2 == 0 ? R.drawable.pending_ui_badge_sage : R.drawable.pending_ui_badge_pink)
        : R.drawable.pending_ui_white);
    ((ImageView) h.icon).setImageResource(completedPage ? R.drawable.pending_ui_ic_check : R.drawable.pending_ui_ic_mail);
    h.icon.setVisibility(overview ? View.GONE : View.VISIBLE);
    h.check.setVisibility(overview ? View.VISIBLE : View.GONE);
    h.check.setOnCheckedChangeListener(null);
    h.check.setChecked(PendingTaskPresentation.completed(t));
    h.check.setEnabled(!PendingTaskPresentation.completed(t));
    h.check.setContentDescription("Mark " + t.getTitle() + " completed");
    h.check.setOnClickListener(
        v -> {
          h.check.setChecked(false);
          actions.complete(t);
        });
    h.itemView.setOnClickListener(v -> v.getContext().startActivity(
        new android.content.Intent(v.getContext(), PendingDetailsActivity.class).putExtra("task_id", t.getId().longValue())));
    h.more.setVisibility(actions == null ? View.GONE : View.VISIBLE);
    h.more.setOnClickListener(
        v -> {
          PopupMenu menu = new PopupMenu(v.getContext(), v);
          menu.getMenu()
              .add("Edit")
              .setOnMenuItemClickListener(
                  x -> {
                    actions.edit(t);
                    return true;
                  });
          if (!PendingTaskPresentation.completed(t))
            menu.getMenu()
                .add("Mark completed")
                .setOnMenuItemClickListener(
                    x -> {
                      actions.complete(t);
                      return true;
                    });
          menu.show();
        });
  }

  static class Holder extends RecyclerView.ViewHolder {
    TextView title, notes, due, meta;
    CheckBox check;
    View icon, more;

    Holder(View v) {
      super(v);
      title = v.findViewById(R.id.pendingUiTaskTitle);
      notes = v.findViewById(R.id.pendingUiTaskNotes);
      meta = v.findViewById(R.id.taskRowMetadata);
      due = v.findViewById(R.id.pendingUiTaskDue);
      check = v.findViewById(R.id.pendingUiTaskCheck);
      icon = v.findViewById(R.id.pendingUiTaskIcon);
      more = v.findViewById(R.id.pendingUiTaskMore);
    }
  }
}
