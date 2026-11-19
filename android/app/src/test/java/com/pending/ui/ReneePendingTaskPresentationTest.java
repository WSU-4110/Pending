
package com.pending.ui;

import static org.junit.Assert.*;

import com.pending.model.PendingItem;
import java.util.*;
import org.junit.Test;

public class ReneePendingTaskPresentationTest {

    private PendingItem task(String title, String due, Integer status) {
        PendingItem item = new PendingItem();
        item.setTitle(title);
        item.setExpectedResponseDate(due);
        item.setStatus(status);
        return item;
    }

    private Date date(int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2026, Calendar.NOVEMBER, day, 12, 0);
        return c.getTime();
    }

    // Test 1: completed()
    @Test
    public void testCompletedStatus() {
        PendingItem item = task("Homework", null, null);

        assertFalse(PendingTaskPresentation.completed(item));

        item.setStatus(2);
        assertFalse(PendingTaskPresentation.completed(item));

        item.setStatus(3);
        assertTrue(PendingTaskPresentation.completed(item));
    }

    // Test 2: active()
    @Test
    public void testActiveTask() {
        PendingItem item = task("Meeting", null, 1);

        assertTrue(PendingTaskPresentation.active(item));

        item.setDeletedAt("");
        assertTrue(PendingTaskPresentation.active(item));

        item.setDeletedAt("2026-11-19T12:00:00Z");
        assertFalse(PendingTaskPresentation.active(item));
    }

    // Test 3: due()
    @Test
    public void testDueDateValidation() {
        PendingItem item = task(
                "Assignment", "2026-11-21T15:00:00Z", 1
        );

        assertEquals(
                "2026-11-21",
                PendingTaskPresentation.due(item)
        );

        item.setExpectedResponseDate("2026-02-30");
        assertEquals("", PendingTaskPresentation.due(item));

        item.setExpectedResponseDate(null);
        assertEquals("", PendingTaskPresentation.due(item));
    }

    // Test 4: dueLabel()
    @Test
    public void testDueDateLabels() {
        Date today = date(19);

        assertEquals(
                "Due today",
                PendingTaskPresentation.dueLabel(
                        task("A", "2026-11-19", 1), today
                )
        );

        assertEquals(
                "Due tomorrow",
                PendingTaskPresentation.dueLabel(
                        task("B", "2026-11-20", 1), today
                )
        );

        assertEquals(
                "Overdue · 2026-11-18",
                PendingTaskPresentation.dueLabel(
                        task("C", "2026-11-18", 1), today
                )
        );

        assertEquals(
                "No due date",
                PendingTaskPresentation.dueLabel(
                        task("D", null, 1), today
                )
        );
    }

    // Test 5: filter()
    @Test
    public void testTaskFiltering() {
        PendingItem open = task("Call supplier", "2026-11-20", 1);
        open.setWaitingOn("Vendor");

        PendingItem completed = task("Finished report", "2026-11-19", 3);

        PendingItem deleted = task("Deleted vendor", "2026-11-18", 1);
        deleted.setDeletedAt("2026-11-19T12:00:00Z");

        List<PendingItem> tasks = Arrays.asList(
                deleted, completed, open
        );

        List<PendingItem> results =
                PendingTaskPresentation.filter(tasks, "vendor", null, 1);

        assertEquals(1, results.size());
        assertSame(open, results.get(0));

        assertEquals(
                1,
                PendingTaskPresentation.filter(tasks, "", null, 2).size()
        );

        assertEquals(
                1,
                PendingTaskPresentation.filter(
                        tasks, "", "2026-11-20", 0
                ).size()
        );
    }

    // Test 6: weekly()
    @Test
    public void testWeeklyProgress() {
        PendingItem done = task("Finished", "2026-11-01", 3);
        done.setResolvedAt("2026-11-18T17:30:00Z");

        PendingItem open = task("Upcoming", "2026-11-20", 1);
        PendingItem old = task("Old task", "2026-11-10", 1);

        PendingItem deleted = task("Removed", "2026-11-19", 1);
        deleted.setDeletedAt("2026-11-19T12:00:00Z");

        List<PendingItem> tasks = Arrays.asList(
                done, open, old, deleted
        );

        assertArrayEquals(
                new int[]{1, 2},
                PendingTaskPresentation.weekly(tasks, date(19))
        );

        assertArrayEquals(
                new int[]{0, 0},
                PendingTaskPresentation.weekly(
                        Collections.emptyList(), date(19)
                )
        );
    }
}
