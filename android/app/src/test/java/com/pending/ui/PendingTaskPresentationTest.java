package com.pending.ui;

import static org.junit.Assert.*;

import com.pending.model.PendingItem;
import java.util.*;
import org.junit.Test;

public class PendingTaskPresentationTest {
  private PendingItem task(String due, int status) {
    PendingItem t = new PendingItem();
    t.setExpectedResponseDate(due);
    t.setStatus(status);
    t.setTitle("Proposal");
    return t;
  }

  private Date now() {
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(2026, Calendar.NOVEMBER, 9, 12, 0);
    return c.getTime();
  }

  @Test
  public void excludesDeletedAndDoesNotInventIntermediateStatuses() {
    PendingItem a = task("2026-11-09", 1), b = task("2026-11-09", 2), c = task("2026-11-09", 3);
    b.setDeletedAt("2026-11-08");
    List<PendingItem> tasks = Arrays.asList(a, b, c);
    assertEquals(1, PendingTaskPresentation.filter(tasks, "", null, 1).size());
    assertEquals(1, PendingTaskPresentation.filter(tasks, "", null, 2).size());
    assertFalse(PendingTaskPresentation.completed(a));
  }

  @Test
  public void filtersNotesCaseInsensitivelyAndByDate() {
    PendingItem a = task("2026-11-09", 0);
    a.setNotes("Call VENDOR");
    assertEquals(
        1, PendingTaskPresentation.filter(Arrays.asList(a), "vendor", "2026-11-09", 0).size());
    assertEquals(
        0, PendingTaskPresentation.filter(Arrays.asList(a), "vendor", "2026-11-10", 0).size());
  }

  @Test
  public void weeklyIncludesCompletedAndOpenButNotOutsideWeek() {
    PendingItem completed = task("2026-11-02", 3);
    completed.setResolvedAt("2026-11-10T12:30:00.123456Z");
    List<PendingItem> tasks =
        Arrays.asList(
            completed, task("2026-11-15", 0), task("2026-11-08", 3), task(null, 0));
    assertArrayEquals(new int[] {1, 2}, PendingTaskPresentation.weekly(tasks, now()));
    assertArrayEquals(
        new int[] {0, 0}, PendingTaskPresentation.weekly(Collections.emptyList(), now()));
  }

  @Test
  public void excludesUndatedDeletedAndPriorWeekCompletions() {
    PendingItem historical = task("2026-11-12", 3);
    PendingItem previous = task("2026-11-12", 4);
    previous.setResolvedAt("2026-11-06T12:00:00Z");
    PendingItem deleted = task(null, 3);
    deleted.setResolvedAt("2026-11-12T12:00:00Z");
    deleted.setDeletedAt("2026-11-13");
    assertArrayEquals(new int[]{0, 0}, PendingTaskPresentation.weekly(
        Arrays.asList(historical, previous, deleted), now()));
  }

  @Test
  public void usesDeviceLocalMondayBoundaryAndCountsOnce() {
    TimeZone original = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
      PendingItem before = task(null, 3), after = task("2026-11-10", 3);
      before.setResolvedAt("2026-11-09T04:59:59Z");
      after.setResolvedAt("2026-11-09T05:00:00Z");
      assertArrayEquals(new int[]{1, 1}, PendingTaskPresentation.weekly(Arrays.asList(before, after), now()));
    } finally {
      TimeZone.setDefault(original);
    }
  }

  @Test
  public void handlesMissingAndInvalidDates() {
    assertEquals("No due date", PendingTaskPresentation.dueLabel(task(null, 0), now()));
    assertEquals("", PendingTaskPresentation.due(task("2026-02-30", 0)));
    assertEquals("Due today", PendingTaskPresentation.dueLabel(task("2026-11-09", 0), now()));
    assertEquals("Due tomorrow", PendingTaskPresentation.dueLabel(task("2026-11-10", 0), now()));
  }
}
