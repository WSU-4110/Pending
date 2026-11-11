package com.pending.command;

import com.pending.model.PendingItem;
import com.pending.repository.NetworkCallback;

/** A task action whose asynchronous result is delivered through the existing callback. */
public interface TaskCommand {
    void execute(NetworkCallback<PendingItem> callback);
}
