package com.example.barberbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Color "Perfect" in gold
        TextView tvHeadline = findViewById(R.id.tvHeadline);
        String full = "Find Your\nPerfect Cut";
        SpannableString ss = new SpannableString(full);
        int start = full.indexOf("Perfect");
        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#C9A84C")),
                start, start + "Perfect".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvHeadline.setText(ss);

        // Animate elements in
        animateIn(findViewById(R.id.flIllustration), 0);
        animateIn(tvHeadline, 120);
        animateIn(findViewById(R.id.tvSubtitle), 220);
        animateIn(findViewById(R.id.btnGetStarted), 340);
        animateIn(findViewById(R.id.tvSkip), 400);

        // Get Started button
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            // Mark onboarded
            SharedPreferences prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("onboarded", true).apply();

            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Skip
        TextView tvSkip = findViewById(R.id.tvSkip);
        tvSkip.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("onboarded", true).apply();
            startActivity(new Intent(this, RecommendationsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void animateIn(View view, long delayMs) {
        view.setAlpha(0f);
        view.setTranslationY(40f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(480)
                .start();
    }
}
