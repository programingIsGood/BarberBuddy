package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity — improved with:
 *  - Real-time field validation
 *  - Loading indicator & button lock during processing
 *  - Forgot Password dialog
 *  - Proper session-aware navigation
 *  - Smooth entry animations
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout    tilEmail, tilPassword;
    private TextInputEditText  etEmail, etPassword;
    private MaterialButton     btnLogin;
    private CircularProgressIndicator progressBar;
    private TextView           tvError, tvRegister, tvForgot, tvGuest;

    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        animateIn();
        setupListeners();
    }

    private void bindViews() {
        tilEmail     = findViewById(R.id.tilEmail);
        tilPassword  = findViewById(R.id.tilPassword);
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        progressBar  = findViewById(R.id.progressBar);
        tvError      = findViewById(R.id.tvError);
        tvRegister   = findViewById(R.id.tvGoRegister);
        tvForgot     = findViewById(R.id.tvForgotPassword);
        tvGuest      = findViewById(R.id.tvGuest);
    }

    private void setupListeners() {
        etEmail.setOnFocusChangeListener((v, f) -> { if (f) clearFieldError(tilEmail); });
        etPassword.setOnFocusChangeListener((v, f) -> { if (f) clearFieldError(tilPassword); });

        btnLogin.setOnClickListener(v -> { if (!isProcessing) attemptLogin(); });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        if (tvForgot != null) tvForgot.setOnClickListener(v -> showForgotPasswordDialog());
        if (tvGuest  != null) tvGuest.setOnClickListener(v -> navigateAfterLogin());
    }

    private void attemptLogin() {
        hideKeyboard();
        clearErrors();

        String email    = text(etEmail);
        String password = text(etPassword);

        boolean valid = true;
        String emailErr = UserManager.validateEmail(email);
        if (emailErr != null) { tilEmail.setError(emailErr); valid = false; }
        if (TextUtils.isEmpty(password)) { tilPassword.setError("Password is required."); valid = false; }
        if (!valid) return;

        setProcessing(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String error = UserManager.login(this, email, password);
            setProcessing(false);
            if (error != null) { showGlobalError(error); shakeButton(); }
            else { Toast.makeText(this, "Welcome back! 👋", Toast.LENGTH_SHORT).show(); navigateAfterLogin(); }
        }, 800);
    }

    private void navigateAfterLogin() {
        Intent next;
        if (!Prefs.isOnboarded(this)) {
            next = new Intent(this, MainActivity.class);
        } else {
            java.util.List<Hairstyle> saved = SavedStylesManager.getSavedStyles(this);
            next = saved.isEmpty() ? new Intent(this, MainActivity.class) : new Intent(this, RecommendationsActivity.class);
        }
        next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(next);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(dialogView)
                .setPositiveButton("Send Reset Link", (d, w) -> {
                    TextInputEditText etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);
                    String resetEmail = etDialogEmail.getText() != null ? etDialogEmail.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(resetEmail)) {
                        Toast.makeText(this, "Reset link sent to " + resetEmail, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setProcessing(boolean processing) {
        isProcessing = processing;
        btnLogin.setEnabled(!processing);
        btnLogin.setText(processing ? "" : "Log In");
        if (progressBar != null) progressBar.setVisibility(processing ? View.VISIBLE : View.GONE);
    }

    private void showGlobalError(String msg) {
        if (tvError == null) return;
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvError.setAlpha(0f);
        tvError.animate().alpha(1f).setDuration(250).start();
    }

    private void clearErrors() {
        if (tvError != null) tvError.setVisibility(View.GONE);
        clearFieldError(tilEmail);
        clearFieldError(tilPassword);
    }

    private void clearFieldError(TextInputLayout til) { if (til != null) til.setError(null); }

    private void shakeButton() {
        btnLogin.animate().translationX(-20f).setDuration(60).withEndAction(() ->
                btnLogin.animate().translationX(20f).setDuration(60).withEndAction(() ->
                        btnLogin.animate().translationX(-10f).setDuration(60).withEndAction(() ->
                                btnLogin.animate().translationX(0f).setDuration(60).start()
                        ).start()
                ).start()
        ).start();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void animateIn() {
        animateView(findViewById(R.id.tvWelcome), 0);
        animateView(findViewById(R.id.tvSubtitle), 80);
        animateView(findViewById(R.id.cardForm), 160);
        animateView(tvGuest, 260);
    }

    private void animateView(View v, long delay) {
        if (v == null) return;
        v.setAlpha(0f); v.setTranslationY(30f);
        v.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(400).start();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
