package com.pending.ui;

import android.content.Context;
import android.content.res.TypedArray;
import com.pending.R;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class PendingProgressRingView extends View {
  private final int trackColor, startColor, endColor, textColor;
  private int percent;
  private boolean unavailable;

  public void setUnavailable() {
    unavailable = true;
    setContentDescription("Task progress unavailable");
    invalidate();
  }

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public PendingProgressRingView(Context c, AttributeSet a) {
    super(c, a);
    TypedArray colors = c.obtainStyledAttributes(a, R.styleable.PendingProgressRingView);
    try {
      trackColor = colors.getColor(R.styleable.PendingProgressRingView_ringTrackColor, 0xFFF9DFE5);
      startColor = colors.getColor(R.styleable.PendingProgressRingView_ringStartColor, 0xFFB5CBAA);
      endColor = colors.getColor(R.styleable.PendingProgressRingView_ringEndColor, 0xFF508661);
      textColor = colors.getColor(R.styleable.PendingProgressRingView_ringTextColor, 0xFF14291D);
    } finally {
      colors.recycle();
    }
  }

  public void setProgress(int p) {
    percent = Math.max(0, Math.min(100, p));
    setContentDescription(percent + " percent completed");
    invalidate();
  }

  @Override
  protected void onDraw(Canvas c) {
    super.onDraw(c);
    float size = Math.min(getWidth(), getHeight()), w = size * .12f;
    RectF oval = new RectF(w, w, getWidth() - w, getHeight() - w);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(w);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setShader(null);
    paint.setColor(trackColor);
    c.drawOval(oval, paint);
    paint.setShader(
        new LinearGradient(
            0, 0, getWidth(), getHeight(), startColor, endColor, Shader.TileMode.CLAMP));
    c.drawArc(oval, -90, 360 * percent / 100f, false, paint);
    paint.setShader(null);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(textColor);
    paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    paint.setTextSize(size * .24f);
    paint.setTextAlign(Paint.Align.CENTER);
    c.drawText(
        unavailable ? "—" : percent + "%",
        getWidth() / 2f,
        getHeight() / 2f - (paint.ascent() + paint.descent()) / 2,
        paint);
  }
}
