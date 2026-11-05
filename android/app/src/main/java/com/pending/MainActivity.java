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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int green = Color.rgb(43, 128, 107);
    private final int ink = Color.rgb(32, 61, 53);
    private final int bg = Color.rgb(247, 250, 248);

    private LinearLayout content;
  
    private PendingRepository repository;
    private SessionManager sessionManager;
    private PendingTaskAdapter overdueAdapter;
    private PendingTaskAdapter upcomingAdapter;


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

    @Override
    protected void onResume() {
        super.onResume();
        if(sessionManager != null && sessionManager.isLoggedIn()) {
            TextView openPendingCount = findViewById(R.id.tasksOpenPending);
            if(openPendingCount != null) {
                loadDashboardData(openPendingCount);
            }
        }
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

        TextView createAccountLink =
                findViewById(R.id.loginToRegisterLink);

        if (createAccountLink != null) {
            createAccountLink.setOnClickListener(v -> showRegisterScreen());
        }

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

                        String firstName = result.getFirstName();

                        getPreferences(MODE_PRIVATE)
                                .edit()
                                .putString("first_name",
                                        firstName != null ? firstName : "")
                                .apply();

                        Toast.makeText(MainActivity.this,
                                "Logged in successfully!",
                                Toast.LENGTH_SHORT).show();

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

        EditText firstNameInput = findViewById(R.id.editTextFirstName);
        EditText lastNameInput = findViewById(R.id.editTextLastName);

        EditText emailInput = findViewById(R.id.editTextEmailAddress);
        EditText passwordInput = findViewById(R.id.editTextPassword);

        TextView passwordRequirements =
                findViewById(R.id.passwordCorrectnessText);

        if (passwordInput != null && passwordRequirements != null) {
            passwordInput.addTextChangedListener(
                    new android.text.TextWatcher() {

                        @Override
                        public void beforeTextChanged(
                                CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(
                                CharSequence s, int start, int before, int count) {

                            String password = s.toString();

                            boolean lengthValid = password.length() >= 13;
                            boolean uppercaseValid =
                                    password.matches("(?s).*[A-Z].*");
                            boolean specialValid =
                                    password.matches("(?s).*[^a-zA-Z0-9].*");

                            passwordRequirements.setText(
                                    (lengthValid ? "✅" : "○") +
                                            "  13 or more characters\n" +
                                            (uppercaseValid ? "✅" : "○") +
                                            "  1 uppercase letter\n" +
                                            (specialValid ? "✅" : "○") +
                                            "  1 special character"
                            );
                        }

                        @Override
                        public void afterTextChanged(android.text.Editable s) {
                        }
                    }
            );
        }

        EditText confirmPasswordInput = findViewById(R.id.editTextConfirmPassword);
        Button confirmCreateAccountButton = findViewById(R.id.confirmCreateAccountButton);
        ImageButton backButton = findViewById(R.id.backButtonLogin);

        TextView loginLink = findViewById(R.id.alreadyRegisteredLink);

        if (loginLink != null) {
            loginLink.setOnClickListener(v -> showLoginScreen());
        }

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

                boolean lengthValid = password.length() >= 13;
                boolean uppercaseValid = password.matches("(?s).*[A-Z].*");
                boolean specialValid =
                        password.matches("(?s).*[^a-zA-Z0-9].*");

                if (!lengthValid || !uppercaseValid || !specialValid) {
                    passwordInput.setError(
                            "Password must have at least 13 characters, " +
                                    "1 uppercase letter, and 1 special character."
                    );
                    passwordInput.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Enter a valid email address");
                    emailInput.requestFocus();
                    return;
                }


                String firstName = firstNameInput.getText().toString().trim();
                String lastName = lastNameInput.getText().toString().trim();

                if (firstName.isEmpty()) {
                    firstNameInput.setError("First name is required");
                    firstNameInput.requestFocus();
                    return;
                }

                if (lastName.isEmpty()) {
                    lastNameInput.setError("Last name is required");
                    lastNameInput.requestFocus();
                    return;
                }


                repository.register(email, password, firstName, lastName, new NetworkCallback<AuthResponse>() {

                    @Override
                    public void onSuccess(AuthResponse result) {

                        getPreferences(MODE_PRIVATE)
                                .edit()
                                .putString("first_name", firstName)
                                .apply();

                        Toast.makeText(
                                MainActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        showDashboardScreen();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                MainActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });
            });
        }
    }

    private void showDashboardScreen() {
        setContentView(R.layout.activity_main);

        TextView dateText = findViewById(R.id.currentDateText);

        if (dateText != null) {
            String currentDate = new java.text.SimpleDateFormat(
                    "EEEE · MMM d",
                    java.util.Locale.getDefault()
            ).format(new java.util.Date());

            dateText.setText(
                    currentDate.toUpperCase(java.util.Locale.getDefault())
            );
        }

        TextView greetingText = findViewById(R.id.morningText);

        String firstName = getPreferences(MODE_PRIVATE)
                .getString("first_name", "");

        int hour = java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY);

        String greeting = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";

        if (greetingText != null) {
            if (firstName != null && !firstName.trim().isEmpty()) {
                greetingText.setText(greeting + ", " + firstName.trim());
            } else {
                greetingText.setText(greeting);
            }
        }

        TextView openPendingCount =
                findViewById(R.id.tasksOpenPending);

        PendingTaskAdapter.OnTaskClickListener editListener = item -> {
            Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
            intent.putExtra("TASK_ID", item.getId());
            intent.putExtra("TASK_TITLE", item.getTitle());
            intent.putExtra("TASK_WAITING_ON", item.getWaitingOn());
            intent.putExtra("TASK_NOTES", item.getNotes());
            if(item.getExpectedResponseDate() != null) {
                intent.putExtra("TASK_DATE", item.getExpectedResponseDate());
            }
            startActivity(intent);
        };

        // Overdue task list
        RecyclerView overdueList =
                findViewById(R.id.overdueRecyclerView);

        overdueAdapter = new PendingTaskAdapter(editListener);

        overdueList.setLayoutManager(
                new LinearLayoutManager(this)
        );
        overdueList.setAdapter(overdueAdapter);

        // Upcoming task list
        RecyclerView upcomingList =
                findViewById(R.id.upcomingRecyclerView);

        upcomingAdapter = new PendingTaskAdapter(editListener);

        upcomingList.setLayoutManager(
                new LinearLayoutManager(this)
        );
        upcomingList.setAdapter(upcomingAdapter);

        // Load tasks from Spring Boot
        loadDashboardData(openPendingCount);

        // Create task intent
        FloatingActionButton addTaskFab = findViewById(R.id.addTaskButton);
        if (addTaskFab != null) {
            addTaskFab.bringToFront();
            addTaskFab.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                startActivity(intent);
            });
        }

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_pending) {
                return true;
            }

            if (id == R.id.nav_completed) {
                showCompletedScreen();
                return true;
            }

            if (id == R.id.nav_settings) {
                showSettingsScreen();
                return true;
            }

            return false;
        });

    }


    private void showSettingsScreen() {
        setContentView(R.layout.activity_settings);

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            getPreferences(MODE_PRIVATE)
                    .edit()
                    .remove("first_name")
                    .apply();

            showLoginScreen();
        });

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottomNavigation);

        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_pending) {
                showDashboardScreen();
                return true;
            }

            if (id == R.id.nav_settings) {
                return true;
            }

            if (id == R.id.nav_completed) {
                showCompletedScreen();
                return true;
            }

            return false;
        });
    }


    private void showCompletedScreen() {
        setContentView(R.layout.activity_completed);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottomNavigation);

        bottomNav.setSelectedItemId(R.id.nav_completed);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_pending) {
                showDashboardScreen();
                return true;
            }

            if (id == R.id.nav_completed) {
                return true;
            }

            if (id == R.id.nav_settings) {
                showSettingsScreen();
                return true;
            }

            return false;
        });
    }

    private void loadDashboardData(TextView openPendingCount) {

        repository.fetchAllItems(
                new NetworkCallback<List<PendingItem>>() {

                    @Override
                    public void onSuccess(List<PendingItem> items) {

                        List<PendingItem> overdueTasks = new ArrayList<>();
                        List<PendingItem> upcomingTasks = new ArrayList<>();

                        SimpleDateFormat dateFormat =
                                new SimpleDateFormat(
                                        "yyyy-MM-dd", Locale.US
                                );

                        String today = dateFormat.format(new Date());

                        // Find the end of the current calendar week
                        Calendar calendar = Calendar.getInstance();

                        int daysUntilWeekEnd =
                                (calendar.getFirstDayOfWeek() + 6
                                        - calendar.get(Calendar.DAY_OF_WEEK)
                                        + 7) % 7;

                        calendar.add(
                                Calendar.DAY_OF_YEAR, daysUntilWeekEnd
                        );

                        String weekEnd =
                                dateFormat.format(calendar.getTime());

                        int thisWeekCount = 0;

                        // Organize tasks
                        for (PendingItem item : items) {

                            // Skip deleted tasks
                            if (item.getDeletedAt() != null &&
                                    !item.getDeletedAt().isEmpty()) {
                                continue;
                            }

                            // Status 3 or higher means completed
                            Integer status = item.getStatus();

                            if (status != null && status >= 3) {
                                continue;
                            }

                            String expectedDate =
                                    item.getExpectedResponseDate();

                            // Backend dates use yyyy-MM-dd
                            if (expectedDate != null &&
                                    expectedDate.length() >= 10) {
                                expectedDate =
                                        expectedDate.substring(0, 10);
                            }

                            if (expectedDate != null &&
                                    expectedDate.compareTo(today) < 0) {

                                overdueTasks.add(item);

                            } else {

                                upcomingTasks.add(item);

                                if (expectedDate != null &&
                                        expectedDate.compareTo(today) >= 0 &&
                                        expectedDate.compareTo(weekEnd) <= 0) {

                                    thisWeekCount++;
                                }
                            }
                        }

                        // Update dashboard statistics
                        int totalOpen = overdueTasks.size()
                                + upcomingTasks.size();

                        if (openPendingCount != null) {
                            openPendingCount.setText(
                                    String.valueOf(totalOpen)
                            );
                        }

                        TextView overdueCount =
                                findViewById(R.id.tasksOverdue);

                        TextView thisWeekCountView =
                                findViewById(R.id.tasksThisWeek);

                        TextView attentionText =
                                findViewById(R.id.tasksAttention);

                        if (overdueCount != null) {
                            overdueCount.setText(
                                    String.valueOf(overdueTasks.size())
                            );
                        }

                        if (thisWeekCountView != null) {
                            thisWeekCountView.setText(
                                    String.valueOf(thisWeekCount)
                            );
                        }

                        if (attentionText != null) {
                            attentionText.setText(
                                    overdueTasks.size()
                                            + " need attention"
                            );
                        }

                        // Update both task lists
                        overdueAdapter.setTasks(overdueTasks);
                        upcomingAdapter.setTasks(upcomingTasks);
                    }

                    @Override
                    public void onError(String errorMessage) {

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to load tasks: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
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
