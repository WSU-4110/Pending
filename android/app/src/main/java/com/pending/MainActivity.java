package com.pending;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.text.InputType;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pending.model.AuthResponse;
import com.pending.model.PendingItem;
import com.pending.network.SessionManager;
import com.pending.repository.NetworkCallback;
import com.pending.repository.PendingRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int green = Color.rgb(43, 128, 107);
    private final int ink = Color.rgb(32, 61, 53);
    private final int bg = Color.rgb(247, 250, 248);

    private LinearLayout content;
  
    private PendingRepository repository;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new PendingRepository(this);
        sessionManager = SessionManager.getInstance(this);

        if (sessionManager.isLoggedIn()) {
            showDashboardScreen();
        } else {
            showLandingScreen();
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


    private void showLandingScreen() {
        setContentView(R.layout.account_page);

        Button createAccountButton = findViewById(R.id.createAccountButton);
        Button loginButton = findViewById(R.id.loginButton);

        if (createAccountButton != null) {
            createAccountButton.setOnClickListener(v -> showRegisterScreen());
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> showLoginScreen());
        }
    }

    private void showLoginScreen() {
        setContentView(R.layout.login_account_page);

        EditText emailInput = findViewById(R.id.editTextEmailAddress2);
        EditText passwordInput = findViewById(R.id.editTextPassword2);
        Button confirmLoginButton = findViewById(R.id.confirmLoginButton);
        ImageButton backButton = findViewById(R.id.backButtonLogin);

        if (backButton != null) {
            backButton.setOnClickListener(v -> showLandingScreen());
        }

        if (confirmLoginButton != null) {
            confirmLoginButton.setOnClickListener(v -> {
                String email = emailInput != null ? emailInput.getText().toString().trim() : "";
                String password = passwordInput != null ? passwordInput.getText().toString() : "";

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                repository.login(email, password, new NetworkCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse result) {
                        Toast.makeText(MainActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                        showDashboardScreen();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    private void showRegisterScreen() {
        setContentView(R.layout.create_account);

        EditText emailInput = findViewById(R.id.editTextEmailAddress);
        EditText passwordInput = findViewById(R.id.editTextPassword);
        EditText confirmPasswordInput = findViewById(R.id.editTextConfirmPassword);
        Button confirmCreateAccountButton = findViewById(R.id.confirmCreateAccountButton);
        ImageButton backButton = findViewById(R.id.backButtonLogin);

        if (backButton != null) {
            backButton.setOnClickListener(v -> showLandingScreen());
        }

        if (confirmCreateAccountButton != null) {
            confirmCreateAccountButton.setOnClickListener(v -> {
                String email = emailInput != null ? emailInput.getText().toString().trim() : "";
                String password = passwordInput != null ? passwordInput.getText().toString() : "";
                String confirmPassword = confirmPasswordInput != null ? confirmPasswordInput.getText().toString() : "";

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                String firstName = email.contains("@") ? email.substring(0, email.indexOf("@")) : "User";
                String lastName = "Account";

                repository.register(email, password, firstName, lastName, new NetworkCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse result) {
                        Toast.makeText(MainActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        showDashboardScreen();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    private void showDashboardScreen() {
        setContentView(R.layout.activity_main);

        TextView openPendingCount = findViewById(R.id.tasksOpenPending);
        FloatingActionButton addTaskFab = findViewById(R.id.addTaskButton);

        loadDashboardData(openPendingCount);

        if (addTaskFab != null) {
            addTaskFab.bringToFront();
            addTaskFab.setOnClickListener(v -> showCreateTaskDialog(openPendingCount));
        }
    }

    private void loadDashboardData(TextView openPendingCount) {
        repository.fetchAllItems(new NetworkCallback<List<PendingItem>>() {
            @Override
            public void onSuccess(List<PendingItem> items) {
                if (openPendingCount != null) {
                    openPendingCount.setText(String.valueOf(items.size()));
                }

                // Populate the fragment containers in activity_main.xml
                FragmentManager fm = getSupportFragmentManager();
                if (!items.isEmpty()) {
                    PendingTaskFragment frag1 = (PendingTaskFragment) fm.findFragmentById(R.id.fragmentContainerView);
                    if (frag1 != null) {
                        frag1.populateTask(items.get(0), item -> showEditTaskDialog(item, openPendingCount));
                    }
                }

                if (items.size() > 1) {
                    PendingTaskFragment frag2 = (PendingTaskFragment) fm.findFragmentById(R.id.fragmentContainerView2);
                    if (frag2 != null) {
                        frag2.populateTask(items.get(1), item -> showEditTaskDialog(item, openPendingCount));
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Keep default layout text
            }
        });
    }

    private void showCreateTaskDialog(TextView countView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Pending Task");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Task Title (e.g. Internship Application)");
        layout.addView(titleInput);

        final EditText waitingOnInput = new EditText(this);
        waitingOnInput.setHint("Waiting On (e.g. Recruiter, Professor)");
        layout.addView(waitingOnInput);

        final EditText notesInput = new EditText(this);
        notesInput.setHint("Notes / Details");
        layout.addView(notesInput);

        builder.setView(layout);

        builder.setPositiveButton("Save Task", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String waitingOn = waitingOnInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String startDate = "2026-10-21";
            String expectedDate = "2026-10-28";
            Integer defaultStatus = 0; // 0 = Waiting on
            Integer defaultPriority = 1;

            repository.createItem(title, waitingOn, startDate, expectedDate, defaultStatus, notes, defaultPriority, new NetworkCallback<PendingItem>() {
                @Override
                public void onSuccess(PendingItem createdItem) {
                    Toast.makeText(MainActivity.this, "Task created: " + createdItem.getTitle(), Toast.LENGTH_SHORT).show();
                    loadDashboardData(countView);
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(MainActivity.this, "Failed to save: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditTaskDialog(PendingItem item, TextView countView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Pending Task");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Title");
        titleInput.setText(item.getTitle());
        layout.addView(titleInput);

        final EditText waitingOnInput = new EditText(this);
        waitingOnInput.setHint("Waiting On");
        waitingOnInput.setText(item.getWaitingOn());
        layout.addView(waitingOnInput);

        final EditText notesInput = new EditText(this);
        notesInput.setHint("Notes");
        notesInput.setText(item.getNotes());
        layout.addView(notesInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            item.setTitle(titleInput.getText().toString().trim());
            item.setWaitingOn(waitingOnInput.getText().toString().trim());
            item.setNotes(notesInput.getText().toString().trim());

            repository.updateItem(item.getId(), item, new NetworkCallback<PendingItem>() {
                @Override
                public void onSuccess(PendingItem updated) {
                    Toast.makeText(MainActivity.this, "Task updated!", Toast.LENGTH_SHORT).show();
                    loadDashboardData(countView);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
