package com.example.barberbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    private TextView tvFaceShape, tvConfidence, tvScanCount, tvSavedCount;
    private MaterialButton btnScan, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);

        // Views
        tvFaceShape  = findViewById(R.id.tvProfileFaceShape);
        tvConfidence = findViewById(R.id.tvProfileConfidence);
        tvScanCount  = findViewById(R.id.tvScanCount);
        tvSavedCount = findViewById(R.id.tvSavedCount);

        btnScan  = findViewById(R.id.btnProfileScan);
        btnReset = findViewById(R.id.btnResetProfile);

        setupClicks();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String lastShape = prefs.getString("last_face_shape", "Not scanned yet");
        int lastConf     = prefs.getInt("last_confidence", 0);
        int scanCount    = prefs.getInt("scan_count", 0);
        int savedCount   = SavedStylesManager.getSavedStyles(this).size();

        tvFaceShape.setText(lastShape);
        tvConfidence.setText(lastConf > 0 ? lastConf + "% match" : "—");
        tvScanCount.setText(String.valueOf(scanCount));
        tvSavedCount.setText(String.valueOf(savedCount));
    }

    private void setupClicks() {

        btnScan.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class))
        );

        tvSavedCount.setOnClickListener(v ->
                startActivity(new Intent(this, SavedStylesActivity.class))
        );

        btnReset.setOnClickListener(v -> {
            prefs.edit()
                    .remove("last_face_shape")
                    .remove("last_confidence")
                    .remove("scan_count")
                    .putBoolean("onboarded", false)
                    .apply();

            startActivity(new Intent(this, SplashActivity.class));
            finishAffinity();
        });
    }

    private void setupBottomNav() {

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_profile);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, RecommendationsActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedStylesActivity.class));
                finish();
                return true;
            }

            return id == R.id.nav_profile;
        });
    }
}