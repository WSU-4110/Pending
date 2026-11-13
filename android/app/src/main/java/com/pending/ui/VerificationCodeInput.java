package com.pending.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.pending.R;

/** Six visual cells backed by one native editable value, selection, keyboard and clipboard. */
public class VerificationCodeInput extends AppCompatEditText {
    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cell = new RectF();
    private final float gap;

    public VerificationCodeInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        gap = dp(6);
        setBackground(null);
        setTypeface(Typeface.MONOSPACE);
        setSingleLine(true);
        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        setTextDirection(TEXT_DIRECTION_LTR);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        float cellWidth = (width - 5 * gap) / 6f;
        Paint glyphPaint = new Paint(getPaint());
        glyphPaint.setLetterSpacing(0);
        // Native text tracking centers one glyph in each cell while keeping editing/paste intact.
        setLetterSpacing((cellWidth + gap - glyphPaint.measureText("0")) / getTextSize());
        setPadding(Math.round(-gap / 2), 0, Math.round(-gap / 2), 0);
    }

    @Override
    protected void onSelectionChanged(int start, int end) {
        super.onSelectionChanged(start, end);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cellWidth = (getWidth() - 5 * gap) / 6f;
        int active = Math.min(5, Math.max(0, getSelectionStart()));
        int saved = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        for (int i = 0; i < 6; i++) {
            cell.set(i * (cellWidth + gap) + dp(1), dp(1),
                    i * (cellWidth + gap) + cellWidth - dp(1), getHeight() - dp(1));
            cellPaint.setStyle(Paint.Style.FILL);
            cellPaint.setColor(ContextCompat.getColor(getContext(),
                    i % 2 == 0 ? R.color.auth_white : R.color.verification_pink));
            canvas.drawRoundRect(cell, dp(16), dp(16), cellPaint);
            cellPaint.setStyle(Paint.Style.STROKE);
            cellPaint.setStrokeWidth(dp(isFocused() && i == active ? 2 : 1));
            cellPaint.setColor(ContextCompat.getColor(getContext(),
                    isFocused() && i == active ? R.color.verification_sage
                            : i % 2 == 0 ? R.color.verification_soft_sage : R.color.auth_accent_pink));
            canvas.drawRoundRect(cell, dp(16), dp(16), cellPaint);
        }
        canvas.restoreToCount(saved);
        super.onDraw(canvas);
    }
}
