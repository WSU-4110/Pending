package com.pending.command;

import com.pending.model.PendingItem;
import com.pending.repository.NetworkCallback;
import com.pending.repository.PendingRepository;
import com.pending.ui.PendingTaskUpdates;

/** Encapsulates the existing request to complete a task without mutating the visible task. */
public final class CompleteTaskCommand implements TaskCommand {
    private final PendingRepository repository;
    private final PendingItem task;

    public CompleteTaskCommand(PendingRepository repository, PendingItem task) {
        this.repository = repository;
        this.task = task;
    }

    @Override
    public void execute(NetworkCallback<PendingItem> callback) {
        PendingItem update = PendingTaskUpdates.copyForUpdate(task);
        update.setStatus(3);
        repository.updateItem(task.getId(), update, callback);
    }
}
