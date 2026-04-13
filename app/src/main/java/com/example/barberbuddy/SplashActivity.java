package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        boolean onboarded = Prefs.isOnboarded(this);

        long delay = onboarded ? 600 : 1800;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent next;
            if (onboarded) {
                next = new Intent(this, OnboardingActivity.class);
            } else {
                next = new Intent(this, MainActivity.class);
            }

            startActivity(next);
            finish();

        }, delay);
    }
}