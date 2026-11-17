package com.pending.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

/** Keeps the verification form scrollable above the IME, including edge-to-edge windows. */
public class VerificationScrollView extends NestedScrollView {
    public VerificationScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewCompat.setOnApplyWindowInsetsListener(this, (view, insets) -> {
            applyInsets(insets);
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                post(() -> {
                    View focused = findFocus();
                    if (focused instanceof VerificationCodeInput) {
                        // Native EditText reveals its text line, which can leave the taller
                        // pastel cells clipped on short screens. Reveal the entire input.
                        focused.requestRectangleOnScreen(
                                new Rect(0, 0, focused.getWidth(), focused.getHeight()), true);
                    }
                });
            }
            return insets;
        });
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        View focused = findFocus();
        if (focused instanceof VerificationCodeInput && child instanceof ViewGroup) {
            // EditText may request its smaller text-line rectangle again after the resize.
            // Keep every such request large enough for the six complete cells.
            Rect cells = new Rect(0, 0, focused.getWidth(), focused.getHeight());
            ((ViewGroup) child).offsetDescendantRectToMyCoords(focused, cells);
            return super.requestChildRectangleOnScreen(child, cells, immediate);
        }
        return super.requestChildRectangleOnScreen(child, rectangle, immediate);
    }

    void applyInsets(WindowInsetsCompat insets) {
        // Apply only the part overlapping this view. Older adjustResize windows already
        // exclude the keyboard and system bars; adding their full insets would count twice.
        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                | WindowInsetsCompat.Type.displayCutout());
        Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
        Point screen = new Point();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRealSize(screen);
        int[] location = new int[2];
        getLocationOnScreen(location);
        int outsideBottom = Math.max(0, screen.y - location[1] - getHeight());
        int outsideRight = Math.max(0, screen.x - location[0] - getWidth());
        setPadding(Math.max(0, bars.left - location[0]), Math.max(0, bars.top - location[1]),
                Math.max(0, bars.right - outsideRight),
                Math.max(0, Math.max(bars.bottom, ime.bottom) - outsideBottom));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Insets can arrive before adjustResize has laid out the new viewport.
        post(() -> ViewCompat.requestApplyInsets(this));
    }
}
