package com.pending.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

/** Keeps the verification form scrollable above the IME, including edge-to-edge windows. */
public class VerificationScrollView extends NestedScrollView {
    private boolean keyboardVisible;
    private int closedScrollY;
    private int transition;
    private final ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = this::updateKeyboard;

    public VerificationScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewCompat.setOnApplyWindowInsetsListener(this, (view, insets) -> {
            applyInsets(insets);
            post(this::updateKeyboard);
            return insets;
        });
    }

    private void updateKeyboard() {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(this);
        if (insets == null) return;
        boolean visible = insets.isVisible(WindowInsetsCompat.Type.ime());
        if (visible == keyboardVisible) return;
        keyboardVisible = visible;
        int current = ++transition;
        post(() -> {
            if (current != transition) return;
            if (keyboardVisible) revealCode();
            else restoreClosedPosition();
        });
    }

    private void restoreClosedPosition() {
        // Leaving the hidden editor focused lets Android scroll back to its caret
        // after the resize. Keep the text/selection, but return focus to the screen.
        int focusability = getDescendantFocusability();
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        requestFocus();
        setDescendantFocusability(focusability);
        scrollTo(0, closedScrollY);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (!keyboardVisible && focused instanceof VerificationCodeInput) closedScrollY = getScrollY();
        super.requestChildFocus(child, focused);
    }

    private void revealCode() {
        View focused = findFocus();
        if (!(focused instanceof VerificationCodeInput)) return;
        Rect cells = new Rect(0, 0, focused.getWidth(), focused.getHeight());
        offsetDescendantRectToMyCoords(focused, cells);
        int top = getScrollY() + getPaddingTop();
        int bottom = getScrollY() + getHeight() - getPaddingBottom();
        int margin = Math.round(8 * getResources().getDisplayMetrics().density);
        if (cells.bottom + margin > bottom) scrollBy(0, cells.bottom + margin - bottom);
        else if (cells.top - margin < top) scrollBy(0, cells.top - margin - top);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        View focused = findFocus();
        // Caret redraws must not undo manual scrolling or keyboard-dismissal restoration.
        // Focus and the keyboard transition reveal the full input explicitly instead.
        if (focused instanceof VerificationCodeInput) return false;
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
        // On pre-30 Android, IME visibility is inferred from the laid-out root window.
        getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        ViewCompat.requestApplyInsets(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        transition++;
        keyboardVisible = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Insets can arrive before adjustResize has laid out the new viewport.
        post(() -> {
            ViewCompat.requestApplyInsets(this);
            updateKeyboard();
        });
    }
}
