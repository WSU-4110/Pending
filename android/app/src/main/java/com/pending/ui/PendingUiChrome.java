package com.pending.ui;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;

/** Window styling scoped to the polished screens; restore before teammate-owned pages. */
final class PendingUiChrome {
  private final Window window;
  private final int status, navigation, flags;
  PendingUiChrome(Activity activity) {
    window=activity.getWindow(); status=window.getStatusBarColor();
    navigation=window.getNavigationBarColor(); flags=window.getDecorView().getSystemUiVisibility();
    int canvas = activity.getColor(com.pending.R.color.pastel_background);
    window.setStatusBarColor(canvas); window.setNavigationBarColor(canvas);
    int light=flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
    if(Build.VERSION.SDK_INT>=26) light |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
    window.getDecorView().setSystemUiVisibility(light);
  }
  void restore(){window.setStatusBarColor(status);window.setNavigationBarColor(navigation);window.getDecorView().setSystemUiVisibility(flags);}
}
