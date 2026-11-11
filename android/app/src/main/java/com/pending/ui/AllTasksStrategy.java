package com.pending.ui;

import com.pending.model.PendingItem;

final class AllTasksStrategy implements TaskStatusFilterStrategy {
  @Override
  public boolean matches(PendingItem task) {
    return true;
  }
}
