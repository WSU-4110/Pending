package com.pending.ui;

import com.google.gson.Gson;
import com.pending.model.PendingItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Keep visible data unchanged until the API confirms an update. */
public final class PendingTaskUpdates {
  private PendingTaskUpdates() {}
  public static PendingItem copyForUpdate(PendingItem original) {
    Gson gson = new Gson();
    PendingItem copy = gson.fromJson(gson.toJson(original), PendingItem.class);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    copy.setUpdatedAt(format.format(new Date()));
    return copy;
  }
}
