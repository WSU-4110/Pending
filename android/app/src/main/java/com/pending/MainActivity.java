package com.pending;

import android.os.Bundle;
import android.view.View;
import java.util.Locale;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.pending.model.AuthResponse;
import com.pending.model.PendingItem;
import com.pending.network.SessionManager;
import com.pending.repository.NetworkCallback;
import com.pending.repository.PendingRepository;

public class MainActivity extends AppCompatActivity {

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

    private com.pending.ui.PendingOverviewController pendingUi;
    private androidx.activity.OnBackPressedCallback pendingBack;

    private void showDashboardScreen() { showPendingScreen(false); }

    private void showPendingScreen(boolean overview) {
        disposePendingScreen();
        if (pendingBack == null) {
            pendingBack = new androidx.activity.OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    showDashboardScreen();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, pendingBack);
        }
        pendingBack.setEnabled(overview);
        pendingUi = new com.pending.ui.PendingOverviewController(this, repository,
            new com.pending.ui.PendingOverviewController.Host() {
                public void home() { showDashboardScreen(); }
                public void overview() { showPendingScreen(true); }
                public void completed() { showCompletedScreen(); }
                public void settings() { showSettingsScreen(); }
                public void create() { startActivity(CreateTaskActivity.newIntent(MainActivity.this, null)); }
                public void edit(PendingItem item) { startActivity(CreateTaskActivity.newIntent(MainActivity.this, item)); }
            }, overview, getPreferences(MODE_PRIVATE).getString("first_name", ""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingUi != null) pendingUi.refresh();
    }

    @Override
    protected void onDestroy() {
        if (pendingUi != null) pendingUi.dispose();
        super.onDestroy();
    }

    private void showSettingsScreen() {
        disposePendingScreen();
        setContentView(R.layout.activity_settings);

        // Match the warm canvas only while Settings is visible; restore other screens unchanged.
        final int previousStatusColor = getWindow().getStatusBarColor();
        final int previousNavigationColor = getWindow().getNavigationBarColor();
        final int previousSystemUi = getWindow().getDecorView().getSystemUiVisibility();
        int canvasColor = androidx.core.content.ContextCompat.getColor(this, R.color.settings_background);
        getWindow().setStatusBarColor(canvasColor);
        getWindow().setNavigationBarColor(canvasColor);
        getWindow().getDecorView().setSystemUiVisibility(previousSystemUi
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        findViewById(R.id.settingsRoot).addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) { }

            @Override
            public void onViewDetachedFromWindow(View view) {
                getWindow().setStatusBarColor(previousStatusColor);
                getWindow().setNavigationBarColor(previousNavigationColor);
                getWindow().getDecorView().setSystemUiVisibility(previousSystemUi);
                view.removeOnAttachStateChangeListener(this);
            }
        });

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        TextView profileName = findViewById(R.id.settingsProfileName);
        TextView profileEmail = findViewById(R.id.settingsProfileEmail);
        TextView avatar = findViewById(R.id.settingsAvatar);
        profileName.setText(TextUtils.isEmpty(name) ? getString(R.string.settings_profile_fallback) : name);
        profileEmail.setText(TextUtils.isEmpty(email) ? getString(R.string.settings_email_fallback) : email);
        StringBuilder initials = new StringBuilder();
        if (!TextUtils.isEmpty(name) && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            initials.appendCodePoint(parts[0].codePointAt(0));
            if (parts.length > 1) {
                initials.appendCodePoint(parts[parts.length - 1].codePointAt(0));
            }
        }
        avatar.setText(initials.length() == 0 ? getString(R.string.settings_avatar_fallback)
                : initials.toString().toUpperCase(Locale.getDefault()));
        TextView version = findViewById(R.id.settingsVersion);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(getString(R.string.settings_version, versionName));
        } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {
            version.setVisibility(View.GONE);
        }

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            getPreferences(MODE_PRIVATE)
                    .edit()
                    .remove("first_name")
                    .apply();

            sessionManager.logout();
            showLoginScreen();
        });



        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                findViewById(R.id.bottomNavigation);

        // The scoped item background highlights icon and label together, as in the references.
        bottomNav.setItemActiveIndicatorEnabled(false);
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
        disposePendingScreen();
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

    private void disposePendingScreen() {
        if (pendingUi != null) {
            pendingUi.dispose();
            pendingUi = null;
        }
        if (pendingBack != null) pendingBack.setEnabled(false);
    }
}
