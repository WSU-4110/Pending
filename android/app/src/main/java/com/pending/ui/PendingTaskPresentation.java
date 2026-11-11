package com.pending.ui;

import com.pending.model.PendingItem;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public final class PendingTaskPresentation {
  private PendingTaskPresentation() {}

  public static String date(Date value) {
    return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(value);
  }

  public static boolean completed(PendingItem t) {
    return t.getStatus() != null && t.getStatus() >= 3;
  }

  public static boolean active(PendingItem t) {
    return t.getDeletedAt() == null || t.getDeletedAt().isEmpty();
  }

  public static String due(PendingItem t) {
    String s = t.getExpectedResponseDate();
    if (s == null || s.length() < 10) return "";
    s = s.substring(0, 10);
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    f.setLenient(false);
    ParsePosition p = new ParsePosition(0);
    return f.parse(s, p) != null && p.getIndex() == 10 ? s : "";
  }

  public static String dueLabel(PendingItem t, Date now) {
    String d = due(t);
    if (d.isEmpty()) return "No due date";
    String today = date(now);
    if (d.equals(today)) return "Due today";
    Calendar c = Calendar.getInstance();
    c.setTime(now);
    c.add(Calendar.DATE, 1);
    if (d.equals(date(c.getTime()))) return "Due tomorrow";
    return (d.compareTo(today) < 0 ? "Overdue · " : "Due ") + d;
  }

  public static List<PendingItem> filter(
      List<PendingItem> tasks, String query, String day, TaskStatusFilterStrategy strategy) {
    List<PendingItem> out = new ArrayList<>();
    String q = query.trim().toLowerCase(Locale.ROOT);
    for (PendingItem t : tasks) {
      if (!active(t) || !strategy.matches(t)) continue;
      if (day != null && !day.equals(due(t))) continue;
      String hay =
          Objects.toString(t.getTitle(), "")
              + " "
              + Objects.toString(t.getNotes(), "")
              + " "
              + Objects.toString(t.getWaitingOn(), "");
      if (hay.toLowerCase(Locale.ROOT).contains(q)) out.add(t);
    }
    out.sort(Comparator.comparing(t -> due(t).isEmpty() ? "9999" : due(t)));
    return out;
  }

  public static int[] weekly(List<PendingItem> tasks, Date now) {
    Calendar c = Calendar.getInstance();
    c.setTime(now);
    int delta = (c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
    c.add(Calendar.DATE, -delta);
    String start = date(c.getTime());
    c.add(Calendar.DATE, 6);
    String end = date(c.getTime());
    int done = 0, total = 0;
    for (PendingItem t : tasks)
      if (active(t)
          && !due(t).isEmpty()
          && due(t).compareTo(start) >= 0
          && due(t).compareTo(end) <= 0) {
        total++;
        if (completed(t)) done++;
      }
    return new int[] {done, total};
  }
}
