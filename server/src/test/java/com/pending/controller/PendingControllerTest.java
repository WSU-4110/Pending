package com.pending.controller;

import com.pending.model.PendingItem;
import com.pending.model.TaskCategory;
import com.pending.repository.PendingItemRepository;
import com.pending.repository.ReminderRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendingControllerTest {
    private final PendingItemRepository items = mock(PendingItemRepository.class);
    private final ReminderRepository reminders = mock(ReminderRepository.class);
    private final PendingController controller = new PendingController(items, reminders);

    @Test void legacyAndExplicitCategoriesSurviveUpdates() {
        PendingItem task = new PendingItem();
        assertEquals(TaskCategory.Other, task.getCategory());
        task.setTitle("Category review");
        task.setCategory(TaskCategory.Work);
        when(items.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(TaskCategory.Work, controller.createItem(task, 12L).getBody().getCategory());
        when(items.findByIdAndOwnerIdAndDeletedAtIsNull(9L, 12L)).thenReturn(Optional.of(task));
        PendingItem edit = new PendingItem();
        edit.setCategory(TaskCategory.Personal);
        assertEquals(TaskCategory.Personal, controller.updateItem(9L, edit, 12L).getBody().getCategory());
        assertEquals(TaskCategory.Personal, controller.updateItem(9L, new PendingItem(), 12L).getBody().getCategory());
    }

    @Test void completionTimeRecordsTheTransitionOnly() {
        PendingItem task = new PendingItem();
        when(items.findByIdAndOwnerIdAndDeletedAtIsNull(7L, 12L)).thenReturn(Optional.of(task));
        when(items.save(any())).thenAnswer(call -> call.getArgument(0));
        PendingItem edit = new PendingItem();
        edit.setStatus(3);
        OffsetDateTime resolved = controller.updateItem(7L, edit, 12L).getBody().getResolvedAt();
        assertNotNull(resolved);
        assertEquals(resolved, controller.updateItem(7L, edit, 12L).getBody().getResolvedAt());
        // A historical completion with no recorded time must stay undated.
        task.setResolvedAt(null);
        assertNull(controller.updateItem(7L, edit, 12L).getBody().getResolvedAt());
    }
}
