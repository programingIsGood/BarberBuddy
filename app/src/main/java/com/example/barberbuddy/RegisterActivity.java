package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * RegisterActivity — improved with:
 *  - Live password strength indicator
 *  - Terms & Conditions / Privacy Policy agreement with modal
 *  - Loading state & button lock
 *  - Per-field error messages
 *  - Auto-login after registration
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout    tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText  etName, etEmail, etPassword, etConfirm;
    private LinearProgressIndicator passwordStrengthBar;
    private TextView           tvPasswordStrengthLabel;
    private CheckBox           cbTerms;
    private TextView           tvTermsLink, tvError, tvLogin;
    private MaterialButton     btnRegister;
    private CircularProgressIndicator progressBar;
    private ImageButton        btnBack;

    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        animateIn();
        setupListeners();
    }

    private void bindViews() {
        tilName                = findViewById(R.id.tilName);
        tilEmail               = findViewById(R.id.tilEmail);
        tilPassword            = findViewById(R.id.tilPassword);
        tilConfirm             = findViewById(R.id.tilConfirm);
        etName                 = findViewById(R.id.etName);
        etEmail                = findViewById(R.id.etEmail);
        etPassword             = findViewById(R.id.etPassword);
        etConfirm              = findViewById(R.id.etConfirm);
        passwordStrengthBar    = findViewById(R.id.passwordStrengthBar);
        tvPasswordStrengthLabel= findViewById(R.id.tvPasswordStrengthLabel);
        cbTerms                = findViewById(R.id.cbTerms);
        tvTermsLink            = findViewById(R.id.tvTermsLink);
        tvError                = findViewById(R.id.tvError);
        tvLogin                = findViewById(R.id.tvGoLogin);
        btnRegister            = findViewById(R.id.btnRegister);
        progressBar            = findViewById(R.id.progressBar);
        btnBack                = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Clear errors on focus
        etName.setOnFocusChangeListener((v, f)    -> { if (f) clearFieldError(tilName); });
        etEmail.setOnFocusChangeListener((v, f)   -> { if (f) clearFieldError(tilEmail); });
        etPassword.setOnFocusChangeListener((v, f)-> { if (f) clearFieldError(tilPassword); });
        etConfirm.setOnFocusChangeListener((v, f) -> { if (f) clearFieldError(tilConfirm); });

        // Live password strength
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                updatePasswordStrength(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Terms link → show modal
        if (tvTermsLink != null) tvTermsLink.setOnClickListener(v -> showTermsModal());

        // Register
        btnRegister.setOnClickListener(v -> { if (!isProcessing) attemptRegister(); });

        // Navigate to Login
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        hideKeyboard();
        clearErrors();

        String name     = text(etName);
        String email    = text(etEmail);
        String password = text(etPassword);
        String confirm  = text(etConfirm);

        boolean valid = true;

        if (name.length() < 2) { tilName.setError("Enter your full name."); valid = false; }

        String emailErr = UserManager.validateEmail(email);
        if (emailErr != null) { tilEmail.setError(emailErr); valid = false; }

        String passErr = UserManager.validatePassword(password);
        if (passErr != null) { tilPassword.setError(passErr); valid = false; }
        else if (!password.equals(confirm)) {
            tilConfirm.setError("Passwords do not match.");
            valid = false;
        }

        if (cbTerms != null && !cbTerms.isChecked()) {
            showGlobalError("You must agree to the Terms & Privacy Policy to continue.");
            valid = false;
        }

        if (!valid) return;

        setProcessing(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String error = UserManager.register(this, name, email, password);
            if (error != null) {
                setProcessing(false);
                showGlobalError(error);
                return;
            }

            // Auto-login
            UserManager.login(this, email, password);
            SessionManager.getInstance(this).setTermsAgreed(true);
            Prefs.setOnboarded(this, true);

            setProcessing(false);
            Toast.makeText(this, "Account created successfully! 🎉", Toast.LENGTH_SHORT).show();

            Intent next = new Intent(this, MainActivity.class);
            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 1000);
    }

    // ── Password Strength ─────────────────────────────────────────────────────

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBar == null || tvPasswordStrengthLabel == null) return;
        int strength = UserManager.passwordStrength(password);
        int progress = strength * 25;
        passwordStrengthBar.setProgress(progress);
        passwordStrengthBar.setVisibility(password.isEmpty() ? View.GONE : View.VISIBLE);
        tvPasswordStrengthLabel.setVisibility(password.isEmpty() ? View.GONE : View.VISIBLE);

        int color;
        String label;
        switch (strength) {
            case 1:  color = 0xFFE53935; label = "Weak";      break;
            case 2:  color = 0xFFFF7043; label = "Fair";      break;
            case 3:  color = 0xFFFDD835; label = "Good";      break;
            case 4:  color = 0xFF43A047; label = "Strong ✓";  break;
            default: color = 0xFFE53935; label = "Too short"; break;
        }
        passwordStrengthBar.setIndicatorColor(color);
        tvPasswordStrengthLabel.setText(label);
        tvPasswordStrengthLabel.setTextColor(color);
    }

    // ── Terms Modal ───────────────────────────────────────────────────────────

    private void showTermsModal() {
        String termsText =
                "TERMS OF SERVICE & PRIVACY POLICY\n\n" +
                        "BarberBuddy collects and processes facial data solely for the purpose " +
                        "of providing personalized hairstyle recommendations.\n\n" +
                        "DATA COLLECTION\n" +
                        "• We analyze facial landmark data using on-device AI.\n" +
                        "• No raw images or videos are stored on our servers.\n" +
                        "• Facial scan results are stored locally on your device only.\n\n" +
                        "DATA USAGE\n" +
                        "• Your data is used only to generate hairstyle recommendations.\n" +
                        "• We do NOT share, sell, or transfer your data to third parties.\n" +
                        "• Aggregated, anonymized analytics may be used to improve the app.\n\n" +
                        "YOUR RIGHTS\n" +
                        "• You can delete all your data at any time from Profile → Delete Account.\n" +
                        "• You may request a copy of your data by contacting support.\n" +
                        "• Camera access is required for the core feature and can be revoked at any time.\n\n" +
                        "SECURITY\n" +
                        "• Passwords are hashed and salted — never stored in plain text.\n" +
                        "• Sessions are managed with secure random tokens.\n" +
                        "• All data transmission uses TLS encryption.\n\n" +
                        "By registering, you confirm you are 13 years of age or older and agree " +
                        "to these terms.";

        new AlertDialog.Builder(this)
                .setTitle("Terms & Privacy Policy")
                .setMessage(termsText)
                .setPositiveButton("I Agree", (d, w) -> {
                    if (cbTerms != null) cbTerms.setChecked(true);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────

    private void setProcessing(boolean processing) {
        isProcessing = processing;
        btnRegister.setEnabled(!processing);
        btnRegister.setText(processing ? "" : "Create Account");
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
        clearFieldError(tilName); clearFieldError(tilEmail);
        clearFieldError(tilPassword); clearFieldError(tilConfirm);
    }

    private void clearFieldError(TextInputLayout til) { if (til != null) til.setError(null); }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void animateIn() {
        animateView(findViewById(R.id.tvHeadline), 0);
        animateView(findViewById(R.id.tvSubtitle), 80);
        animateView(findViewById(R.id.cardForm),   160);
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
