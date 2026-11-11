package com.pending.ui;

import com.pending.model.PendingItem;

public interface TaskStatusFilterStrategy {
  boolean matches(PendingItem task);
}
