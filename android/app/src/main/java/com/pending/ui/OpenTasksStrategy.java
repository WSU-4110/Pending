package com.pending.ui;

import com.pending.model.PendingItem;

final class OpenTasksStrategy implements TaskStatusFilterStrategy {
  @Override
  public boolean matches(PendingItem task) {
    return !PendingTaskPresentation.completed(task);
  }
}
