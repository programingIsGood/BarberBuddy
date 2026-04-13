package com.example.barberbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);

        // Load last face scan result
        String lastShape   = prefs.getString("last_face_shape", "Not scanned yet");
        int    lastConf    = prefs.getInt("last_confidence", 0);
        int    scanCount   = prefs.getInt("scan_count", 0);
        int    savedCount  = SavedStylesManager.getSavedStyles(this).size();

        TextView tvFaceShape   = findViewById(R.id.tvProfileFaceShape);
        TextView tvConfidence  = findViewById(R.id.tvProfileConfidence);
        TextView tvScanCount   = findViewById(R.id.tvScanCount);
        TextView tvSavedCount  = findViewById(R.id.tvSavedCount);
        MaterialButton btnScan = findViewById(R.id.btnProfileScan);
        MaterialButton btnReset= findViewById(R.id.btnResetProfile);

        tvFaceShape.setText(lastShape);
        tvConfidence.setText(lastConf > 0 ? lastConf + "% match" : "—");
        tvScanCount.setText(String.valueOf(scanCount));
        tvSavedCount.setText(String.valueOf(savedCount));

        btnScan.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

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

        // Bottom nav
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.getMenu().findItem(R.id.nav_profile).setChecked(true);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, RecommendationsActivity.class));
                finish();
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            return true;
        });
    }
}
