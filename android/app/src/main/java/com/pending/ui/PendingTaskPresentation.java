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

  public static String completionDate(PendingItem task) {
    String value = task.getResolvedAt();
    if (value == null) return "";
    // Parse the offset supplied by Spring before formatting in the device time zone.
    java.util.regex.Matcher parts = java.util.regex.Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})$").matcher(value);
    if (!parts.matches()) return "";
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
    format.setLenient(false);
    try {
      return date(format.parse(parts.group(1) + parts.group(2)));
    } catch (java.text.ParseException invalid) {
      return "";
    }
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
      List<PendingItem> tasks, String query, String day, int state) {
    List<PendingItem> out = new ArrayList<>();
    String q = query.trim().toLowerCase(Locale.ROOT);
    for (PendingItem t : tasks) {
      if (!active(t) || (state == 1 && completed(t)) || (state == 2 && !completed(t))) continue;
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
    // Completed work belongs to the week it was resolved. Open work belongs to its due week.
    for (PendingItem task : tasks) {
      if (!active(task)) continue;
      boolean resolved = completed(task);
      String day = resolved ? completionDate(task) : due(task);
      if (day.compareTo(start) < 0 || day.compareTo(end) > 0) continue;
      total++;
      if (resolved) done++;
    }
    return new int[] {done, total};
  }
}
