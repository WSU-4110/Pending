package com.pending;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int green = Color.rgb(43, 128, 107);
    private final int ink = Color.rgb(32, 61, 53);
    private final int bg = Color.rgb(247, 250, 248);

    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferences(MODE_PRIVATE)
                .getBoolean("logged_in", false)) {
            showDashboard();
        } else {
            showWelcome();
        }
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }

    private void screen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(24), dp(44), dp(24), dp(24)
        );

        scroll.addView(content);
        setContentView(scroll);
    }

    private TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this);

        t.setText(value);
        t.setTextColor(ink);
        t.setTextSize(size);

        if (bold) {
            t.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        t.setPadding(0, dp(8), 0, dp(8));
        content.addView(t);

        return t;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);

        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);

        b.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(green)
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(54));

        p.topMargin = dp(12);
        content.addView(b, p);

        b.setOnClickListener(v -> action.run());

        return b;
    }

    private EditText field(String hint, boolean password) {
        EditText e = new EditText(this);

        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextColor(ink);
        e.setHintTextColor(0xFF7C948C);

        e.setInputType(
                password
                        ? InputType.TYPE_CLASS_TEXT |
                          InputType.TYPE_TEXT_VARIATION_PASSWORD
                        : InputType.TYPE_CLASS_TEXT |
                          InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(58));

        p.topMargin = dp(10);
        content.addView(e, p);

        return e;
    }

    private void showWelcome() {
        screen();

        text("Pending", 36, true);

        text(
                "Keep track of everything you're waiting on.",
                19,
                false
        );

        text(
                "Stay organized. Follow up. Get things done.",
                15,
                false
        );

        button("Get Started", this::showLogin);
    }

    private void showLogin() {
        screen();

        text("Welcome back", 30, true);
        text("Sign in to view your pending tasks", 16, false);

        EditText email = field("Email", false);
        EditText password = field("Password", true);

        button("Log In", () -> {

            String enteredEmail =
                    email.getText().toString().trim();

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(enteredEmail).matches()) {
                email.setError("Enter a valid email");
                return;
            }

            if (password.getText().toString().isEmpty()) {
                password.setError("Enter a password");
                return;
            }

            // Demo only: no real authentication.
            getPreferences(MODE_PRIVATE)
                    .edit()
                    .putBoolean("logged_in", true)
                    .apply();

            showDashboard();
        });

        text(
                "Demo login only — backend authentication is not connected.",
                12,
                false
        );
    }

    private void showDashboard() {
        screen();

        text("My Pending Tasks", 30, true);

        text(
                "Everything you're waiting on, in one place.",
                15,
                false
        );

        button("+ Create Pending Task", () -> {
            startActivity(
                    new Intent(this, CreateTaskActivity.class)
            );
        });

        List<LocalTaskStore.Task> tasks =
                LocalTaskStore.all(this);

        if (tasks.isEmpty()) {
            text(
                    "No pending tasks yet. Create your first task!",
                    17,
                    false
            );
        }

        for (LocalTaskStore.Task task : tasks) {

            String label =
                    (task.completed ? "✓  " : "○  ") +
                            task.title +
                            "\nExpected: " + task.date;

            Button row = button(label, () -> {
                Intent intent = new Intent(
                        this,
                        TaskDetailsActivity.class
                );

                intent.putExtra("task_id", task.id);
                startActivity(intent);
            });

            row.setTextColor(ink);

            row.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            0xFFEAF4EF
                    )
            );

            row.setGravity(
                    Gravity.CENTER_VERTICAL | Gravity.START
            );

            row.setPadding(dp(16), 0, dp(16), 0);
        }

        button("Log Out", () -> {
            getPreferences(MODE_PRIVATE)
                    .edit()
                    .putBoolean("logged_in", false)
                    .apply();

            showWelcome();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (content != null &&
                getPreferences(MODE_PRIVATE)
                        .getBoolean("logged_in", false)) {
            showDashboard();
        }
    }
}
