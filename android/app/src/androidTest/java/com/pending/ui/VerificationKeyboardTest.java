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
                View button = activity.findViewById(R.id.confirmEmailVerificationButton);
                original[0] = button.getTop();
                original[1] = scroll.getChildAt(0).getHeight();
                EditText input = activity.findViewById(R.id.editInboxCodeNumberSigned);
                input.requestFocus();
                ViewCompat.getWindowInsetsController(input).show(WindowInsetsCompat.Type.ime());
            });
            awaitKeyboard(scenario, true);
            SystemClock.sleep(600);
            capture("open-input");
            scenario.onActivity(activity -> {
                NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                EditText input = activity.findViewById(R.id.editInboxCodeNumberSigned);
                Rect visible = new Rect();
                assertTrue(input.getGlobalVisibleRect(visible));
                assertEquals("All code boxes must be visible above the keyboard: " + visible
                        + " scrollY=" + scroll.getScrollY() + " height=" + scroll.getHeight()
                        + " padding=" + scroll.getPaddingBottom(), input.getHeight(), visible.height());
                input.setText("123456");
                assertEquals("123456", input.getText().toString());
                assertEquals(original[0], activity.findViewById(R.id.confirmEmailVerificationButton).getTop());
                assertEquals(original[1], scroll.getChildAt(0).getHeight());
            });
            instrumentation.waitForIdleSync();
            SystemClock.sleep(300);
            scenario.onActivity(activity -> ((NestedScrollView) activity.findViewById(
                    R.id.verificationScroll)).fullScroll(View.FOCUS_DOWN));
            SystemClock.sleep(600);
            capture("open-scrolled");
            scenario.onActivity(activity -> {
                View button = activity.findViewById(R.id.confirmEmailVerificationButton);
                Rect visible = new Rect();
                assertTrue(button.getGlobalVisibleRect(visible));
                assertEquals(button.getHeight(), visible.height());
                Rect window = new Rect();
                button.getWindowVisibleDisplayFrame(window);
                assertTrue("Verify must be above the keyboard", visible.bottom <= window.bottom);
                ViewCompat.getWindowInsetsController(button).hide(WindowInsetsCompat.Type.ime());
            });
            awaitKeyboard(scenario, false);
            scenario.onActivity(activity -> {
                NestedScrollView scroll = activity.findViewById(R.id.verificationScroll);
                scroll.scrollTo(0, 0);
                assertEquals(original[0], activity.findViewById(R.id.confirmEmailVerificationButton).getTop());
                assertEquals("123456", ((EditText) activity.findViewById(R.id.editInboxCodeNumberSigned)).getText().toString());
            });
        }
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
