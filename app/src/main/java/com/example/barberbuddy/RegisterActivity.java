package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton       btnBack   = findViewById(R.id.btnBack);
        TextInputEditText etName    = findViewById(R.id.etName);
        TextInputEditText etEmail   = findViewById(R.id.etEmail);
        TextInputEditText etPass    = findViewById(R.id.etPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirm);
        MaterialButton    btnReg    = findViewById(R.id.btnRegister);
        TextView          tvError   = findViewById(R.id.tvError);
        TextView          tvLogin   = findViewById(R.id.tvGoLogin);

        // Animate in
        animateIn(findViewById(R.id.tvHeadline), 0);
        animateIn(findViewById(R.id.tvSubtitle), 80);
        animateIn(findViewById(R.id.cardForm), 160);

        btnBack.setOnClickListener(v -> finish());

        btnReg.setOnClickListener(v -> {
            String name    = text(etName);
            String email   = text(etEmail);
            String pass    = text(etPass);
            String confirm = text(etConfirm);

            // Validate passwords match before calling manager
            if (!pass.equals(confirm)) {
                showError(tvError, "Passwords do not match.");
                return;
            }

            String error = UserManager.register(this, name, email, pass);
            if (error != null) {
                showError(tvError, error);
                return;
            }

            // Auto-login after registration
            UserManager.login(this, email, pass);

            // Mark onboarded so next splash goes home, not onboarding
            Prefs.setOnboarded(this, true);

            // Go to main scan screen for first-time user
            Intent next = new Intent(this, MainActivity.class);
            next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        tvLogin.setOnClickListener(v -> {
            // Go back to login, clear this from stack
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void animateIn(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(30f);
        view.animate().alpha(1f).translationY(0f)
                .setStartDelay(delay).setDuration(400).start();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showError(TextView tv, String msg) {
        tv.setText(msg);
        tv.setVisibility(View.VISIBLE);
    }
}
