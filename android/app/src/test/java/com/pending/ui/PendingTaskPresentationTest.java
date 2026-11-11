package com.pending.ui;

import static org.junit.Assert.*;

import com.pending.model.PendingItem;
import java.util.*;
import org.junit.Test;

public class PendingTaskPresentationTest {
  private static final TaskStatusFilterStrategy ALL = new AllTasksStrategy();
  private static final TaskStatusFilterStrategy OPEN = new OpenTasksStrategy();
  private static final TaskStatusFilterStrategy COMPLETED = new CompletedTasksStrategy();

  private PendingItem task(String due, Integer status) {
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
    assertEquals(Collections.singletonList(a), PendingTaskPresentation.filter(tasks, "", null, OPEN));
    assertEquals(Collections.singletonList(c), PendingTaskPresentation.filter(tasks, "", null, COMPLETED));
    assertFalse(PendingTaskPresentation.completed(a));
  }

  @Test
  public void filtersNotesCaseInsensitivelyAndByDate() {
    PendingItem a = task("2026-11-09", 0);
    a.setNotes("Call VENDOR");
    assertEquals(
        1, PendingTaskPresentation.filter(Arrays.asList(a), "vendor", "2026-11-09", ALL).size());
    assertEquals(
        0, PendingTaskPresentation.filter(Arrays.asList(a), "vendor", "2026-11-10", ALL).size());
  }

  @Test
  public void partitionsActiveTasksAtTheCompletionThreshold() {
    PendingItem unknown = task(null, null), open = task(null, 2), done = task(null, 3);
    PendingItem later = task(null, 4), deletedOpen = task(null, 0), deletedDone = task(null, 3);
    open.setDeletedAt("");
    deletedOpen.setDeletedAt("2026-11-08");
    deletedDone.setDeletedAt("2026-11-08");
    List<PendingItem> tasks = Arrays.asList(deletedOpen, unknown, done, open, deletedDone, later);
    assertEquals(Arrays.asList(unknown, done, open, later),
        PendingTaskPresentation.filter(tasks, "", null, ALL));
    assertEquals(Arrays.asList(unknown, open), PendingTaskPresentation.filter(tasks, "", null, OPEN));
    assertEquals(Arrays.asList(done, later), PendingTaskPresentation.filter(tasks, "", null, COMPLETED));
  }

  @Test
  public void consultsSuppliedStrategyOnlyForActiveTasks() {
    PendingItem first = task(null, 0), second = task(null, 0), deleted = task(null, 0);
    deleted.setDeletedAt("2026-11-08");
    List<PendingItem> visited = new ArrayList<>();
    TaskStatusFilterStrategy onlySecond = task -> {
      visited.add(task);
      return task == second;
    };
    assertEquals(Collections.singletonList(second),
        PendingTaskPresentation.filter(Arrays.asList(first, deleted, second), "", null, onlySecond));
    assertEquals(Arrays.asList(first, second), visited);
  }

  @Test
  public void combinesStatusDayAndTrimmedSearchAcrossTitleAndWaitingOn() {
    String day = "2026-11-09";
    PendingItem title = task(day, 2), waiting = task(day + "T12:00:00Z", null);
    PendingItem otherDay = task("2026-11-10", 1), done = task(day, 3), unrelated = task(day, 1);
    title.setTitle("Vendor contract");
    waiting.setTitle(null);
    waiting.setWaitingOn("ACME VENDOR");
    otherDay.setTitle("Vendor visit");
    done.setNotes("Vendor");
    List<PendingItem> tasks = Arrays.asList(title, otherDay, waiting, done, unrelated);
    assertEquals(Arrays.asList(title, waiting),
        PendingTaskPresentation.filter(tasks, "  vEnDoR  ", day, OPEN));
  }

  @Test
  public void sortsStablyByDueDateWithoutReorderingSource() {
    PendingItem invalid = task("2026-02-30", 0), missing = task(null, 3);
    PendingItem first = task("2026-11-09", 3), tied = task("2026-11-09", 0);
    PendingItem later = task("2026-11-10", 2);
    List<PendingItem> tasks = Arrays.asList(invalid, later, first, missing, tied);
    List<PendingItem> original = new ArrayList<>(tasks);
    assertEquals(Arrays.asList(first, tied, later, invalid, missing),
        PendingTaskPresentation.filter(tasks, "", null, ALL));
    assertEquals(original, tasks);
  }

  @Test
  public void weeklyIncludesCompletedAndOpenButNotOutsideWeek() {
    List<PendingItem> tasks =
        Arrays.asList(
            task("2026-11-09", 3), task("2026-11-15", 0), task("2026-11-08", 3), task(null, 0));
    assertArrayEquals(new int[] {1, 2}, PendingTaskPresentation.weekly(tasks, now()));
    assertArrayEquals(
        new int[] {0, 0}, PendingTaskPresentation.weekly(Collections.emptyList(), now()));
  }

  @Test
  public void handlesMissingAndInvalidDates() {
    assertEquals("No due date", PendingTaskPresentation.dueLabel(task(null, 0), now()));
    assertEquals("", PendingTaskPresentation.due(task("2026-02-30", 0)));
    assertEquals("Due today", PendingTaskPresentation.dueLabel(task("2026-11-09", 0), now()));
    assertEquals("Due tomorrow", PendingTaskPresentation.dueLabel(task("2026-11-10", 0), now()));
  }
}
