package com.pending.ui;

import com.pending.MainActivity;
import com.pending.R;

import static org.junit.Assert.*;

import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import android.widget.EditText;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VerificationKeyboardTest {
    @Test
    public void overlappingInsetsAreAppliedButResizedKeyboardIsNotCountedTwice() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            android.content.Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            com.pending.ui.VerificationScrollView scroll = new com.pending.ui.VerificationScrollView(context, null);
            android.graphics.Point screen = new android.graphics.Point();
            ((android.view.WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getRealSize(screen);
            WindowInsetsCompat open = new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), androidx.core.graphics.Insets.of(0, 24, 0, 48))
                    .setInsets(WindowInsetsCompat.Type.ime(), androidx.core.graphics.Insets.of(0, 0, 0, 400))
                    .build();
            scroll.layout(0, 0, screen.x, screen.y);
            scroll.applyInsets(open);
            assertEquals(24, scroll.getPaddingTop());
            assertEquals(400, scroll.getPaddingBottom());
            scroll.layout(0, 0, screen.x, screen.y - 400);
            scroll.applyInsets(open);
            assertEquals(0, scroll.getPaddingBottom());
            scroll.layout(0, 0, screen.x, screen.y);
            WindowInsetsCompat closed = new WindowInsetsCompat.Builder(open)
                    .setInsets(WindowInsetsCompat.Type.ime(), androidx.core.graphics.Insets.NONE).build();
            scroll.applyInsets(closed);
            assertEquals(48, scroll.getPaddingBottom());
        });
    }

    @Test
    public void keyboardLeavesCodeAndActionsReachableWithoutReflow() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Method show = MainActivity.class.getDeclaredMethod(
                            "showVerifyEmailScreen", String.class);
                    show.setAccessible(true);
                    show.invoke(activity, "keyboard@example.com");
                } catch (Exception e) { throw new AssertionError(e); }
            });
            instrumentation.waitForIdleSync();
            SystemClock.sleep(600);
            capture("closed");
            int[] original = new int[2];
            scenario.onActivity(activity -> {
                NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                original[0] = activity.findViewById(R.id.confirmEmailVerificationButton).getTop();
                original[1] = scroll.getChildAt(0).getHeight();
            });
            for (int cycle = 0; cycle < 3; cycle++) {
                scenario.onActivity(activity -> {
                    EditText input = activity.findViewById(R.id.editInboxCodeNumberSigned);
                    input.requestFocus();
                    ViewCompat.getWindowInsetsController(input).show(WindowInsetsCompat.Type.ime());
                });
                awaitKeyboard(scenario, true);
                SystemClock.sleep(700);
                scenario.onActivity(activity -> {
                    NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                    EditText input = activity.findViewById(R.id.editInboxCodeNumberSigned);
                    assertFullyVisible(input);
                    input.setText("123456");
                    assertEquals(original[0], activity.findViewById(R.id.confirmEmailVerificationButton).getTop());
                    assertEquals(original[1], scroll.getChildAt(0).getHeight());
                });
                instrumentation.waitForIdleSync();
                SystemClock.sleep(300);
                if (cycle == 0) capture("open-input");
                // Real touch scrolling, rather than fullScroll(), keeps the code input focused.
                swipeToVerify(scenario, instrumentation);
                if (cycle == 0) capture("open-scrolled");
                int[] userScroll = new int[1];
                scenario.onActivity(activity -> {
                    View button = activity.findViewById(R.id.confirmEmailVerificationButton);
                    assertFullyVisible(button);
                    EditText input = activity.findViewById(R.id.editInboxCodeNumberSigned);
                    assertTrue("Scrolling must retain the OTP focus", input.hasFocus());
                    NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                    userScroll[0] = scroll.getScrollY();
                    input.setSelection(3);
                    // Repeated inset/caret events must not pull a user back from Verify.
                    ViewCompat.requestApplyInsets(scroll);
                });
                SystemClock.sleep(800);
                scenario.onActivity(activity -> {
                    assertEquals("Manual scroll must remain stable", userScroll[0],
                            ((NestedScrollView) activity.findViewById(R.id.verificationScroll)).getScrollY());
                    assertFullyVisible(activity.findViewById(R.id.confirmEmailVerificationButton));
                    assertKeyboard(activity, true);
                });
                if (cycle == 2) instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
                else scenario.onActivity(activity -> ViewCompat.getWindowInsetsController(
                        activity.findViewById(R.id.verificationScroll)).hide(WindowInsetsCompat.Type.ime()));
                awaitKeyboard(scenario, false);
                SystemClock.sleep(700);
                scenario.onActivity(activity -> {
                    NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                    assertEquals("Dismissal restores the pre-keyboard position", 0, scroll.getScrollY());
                    assertEquals(original[0], activity.findViewById(R.id.confirmEmailVerificationButton).getTop());
                    assertEquals("123456", ((EditText) activity.findViewById(R.id.editInboxCodeNumberSigned)).getText().toString());
                });
                if (cycle == 0) capture("closed-restored");
            }
        }
    }

    private void assertFullyVisible(View view) {
        Rect visible = new Rect();
        assertTrue(view.getGlobalVisibleRect(visible));
        assertEquals("Full view height must be visible: " + visible, view.getHeight(), visible.height());
        assertEquals("All six cells must fit horizontally", view.getWidth(), visible.width());
        Rect window = new Rect();
        view.getWindowVisibleDisplayFrame(window);
        assertTrue("View must be above the keyboard", visible.bottom <= window.bottom);
    }

    private void assertKeyboard(MainActivity activity, boolean visible) {
        assertEquals(visible, ViewCompat.getRootWindowInsets(activity.findViewById(
                R.id.verificationScroll)).isVisible(WindowInsetsCompat.Type.ime()));
    }

    private void swipeToVerify(ActivityScenario<MainActivity> scenario, Instrumentation instrumentation) {
        for (int attempt = 0; attempt < 5; attempt++) {
            boolean[] visible = {false};
            float[] points = new float[3];
            scenario.onActivity(activity -> {
                View button = activity.findViewById(R.id.confirmEmailVerificationButton);
                Rect rect = new Rect();
                visible[0] = button.getGlobalVisibleRect(rect) && rect.height() == button.getHeight();
                View scroll = activity.findViewById(R.id.verificationScroll);
                int[] location = new int[2];
                scroll.getLocationOnScreen(location);
                points[0] = location[0] + scroll.getWidth() / 2f;
                points[1] = location[1] + scroll.getHeight() * .85f;
                points[2] = location[1] + scroll.getHeight() * .2f;
            });
            if (visible[0]) return;
            long start = SystemClock.uptimeMillis();
            for (int step = 0; step <= 20; step++) {
                int action = step == 0 ? android.view.MotionEvent.ACTION_DOWN
                        : step == 20 ? android.view.MotionEvent.ACTION_UP : android.view.MotionEvent.ACTION_MOVE;
                android.view.MotionEvent event = android.view.MotionEvent.obtain(start, SystemClock.uptimeMillis(),
                        action, points[0], points[1] + (points[2] - points[1]) * step / 20f, 0);
                instrumentation.sendPointerSync(event);
                event.recycle();
                SystemClock.sleep(20);
            }
            SystemClock.sleep(700);
        }
        fail("Could not scroll Verify into view");
    }

    private void capture(String state) throws Exception {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("screenshots"))) return;
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        java.io.File file = new java.io.File(instrumentation.getTargetContext().getExternalFilesDir(null),
                "verification-" + state + ".png");
        // Capture the full physical display; UiAutomation.takeScreenshot crops on API 28
        // when the emulator uses a smaller logical display for compact-screen testing.
        try (android.os.ParcelFileDescriptor descriptor = instrumentation.getUiAutomation()
                .executeShellCommand("screencap -p");
             java.io.FileInputStream input = new java.io.FileInputStream(descriptor.getFileDescriptor());
             java.io.FileOutputStream output = new java.io.FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        }
    }

    private void awaitKeyboard(ActivityScenario<MainActivity> scenario, boolean shown) {
        boolean[] matches = {false};
        for (int attempt = 0; attempt < 150; attempt++) {
            scenario.onActivity(activity -> {
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(
                        activity.findViewById(R.id.verificationScroll));
                matches[0] = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime()) == shown;
            });
            if (matches[0]) return;
            SystemClock.sleep(200);
        }
        fail("Keyboard did not become " + (shown ? "visible" : "hidden"));
    }
}
