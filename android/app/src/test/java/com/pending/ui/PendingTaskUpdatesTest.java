package com.pending.ui;
import org.junit.Test;
import static org.junit.Assert.*;
import com.google.gson.Gson;
import com.pending.model.PendingItem;
public class PendingTaskUpdatesTest {
  @Test public void updatePreservesFieldsAndDoesNotMutateVisibleTask() {
    PendingItem source = new Gson().fromJson("{\"id\":8,\"ownerId\":3,\"title\":\"Review\",\"status\":1,\"priority\":2,\"expectedResponseDate\":\"2026-11-10\",\"notes\":\"Keep me\",\"updatedAt\":\"2026-01-01T00:00:00Z\"}",PendingItem.class);
    PendingItem update=PendingTaskUpdates.copyForUpdate(source);update.setStatus(3);update.setTitle("Changed");
    assertEquals(Integer.valueOf(1),source.getStatus());assertEquals("Review",source.getTitle());assertEquals("2026-01-01T00:00:00Z",source.getUpdatedAt());
    assertEquals(source.getId(),update.getId());assertEquals(source.getOwnerId(),update.getOwnerId());assertEquals(source.getNotes(),update.getNotes());assertEquals(source.getExpectedResponseDate(),update.getExpectedResponseDate());assertEquals(source.getPriority(),update.getPriority());
    assertTrue(new Gson().toJson(update).contains("\"updatedAt\""));assertTrue(update.getUpdatedAt().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
  }
}
