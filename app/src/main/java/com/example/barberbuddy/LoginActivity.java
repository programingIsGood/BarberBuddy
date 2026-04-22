package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText etEmail    = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton    btnLogin   = findViewById(R.id.btnLogin);
        TextView          tvError    = findViewById(R.id.tvError);
        TextView          tvRegister = findViewById(R.id.tvGoRegister);
        TextView          tvGuest    = findViewById(R.id.tvGuest);

        // Animate in
        animateIn(findViewById(R.id.tvWelcome), 0);
        animateIn(findViewById(R.id.tvSubtitle), 80);
        animateIn(findViewById(R.id.cardForm), 160);
        animateIn(tvGuest, 260);

        btnLogin.setOnClickListener(v -> {
            String email = text(etEmail);
            String pass  = text(etPassword);

            String error = UserManager.login(this, email, pass);
            if (error != null) {
                showError(tvError, error);
                return;
            }

            // Success — decide where to go
            navigateAfterLogin();
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvGuest.setOnClickListener(v -> navigateAfterLogin());
    }

    private void navigateAfterLogin() {
        boolean onboarded = Prefs.isOnboarded(this);
        Intent next;

        if (!onboarded) {
            next = new Intent(this, MainActivity.class);
        } else {
            java.util.List<Hairstyle> saved = SavedStylesManager.getSavedStyles(this);
            next = saved.isEmpty()
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, RecommendationsActivity.class);
        }

        startActivity(next);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
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
