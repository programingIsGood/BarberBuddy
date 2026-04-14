package com.example.barberbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);

        // 1. Load Data
        String lastShape   = prefs.getString("last_face_shape", "Not scanned yet");
        int    lastConf    = prefs.getInt("last_confidence", 0);
        int    scanCount   = prefs.getInt("scan_count", 0);

        // Ensure SavedStylesManager is implemented to return a List
        int    savedCount  = SavedStylesManager.getSavedStyles(this).size();

        // 2. Initialize Views
        TextView tvFaceShape   = findViewById(R.id.tvProfileFaceShape);
        TextView tvConfidence  = findViewById(R.id.tvProfileConfidence);
        TextView tvScanCount   = findViewById(R.id.tvScanCount);
        TextView tvSavedCount  = findViewById(R.id.tvSavedCount);
        MaterialButton btnScan = findViewById(R.id.btnProfileScan);
        MaterialButton btnReset= findViewById(R.id.btnResetProfile);

        // Optional: Make the saved stat card clickable
        // Note: You may need to add this ID to your XML CardView for the saved stat
        MaterialCardView cardSaved = (MaterialCardView) tvSavedCount.getParent().getParent();

        // 3. Set Text
        tvFaceShape.setText(lastShape);
        tvConfidence.setText(lastConf > 0 ? lastConf + "% match" : "—");
        tvScanCount.setText(String.valueOf(scanCount));
        tvSavedCount.setText(String.valueOf(savedCount));

        // 4. Click Listeners
        btnScan.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        cardSaved.setOnClickListener(v -> {
            startActivity(new Intent(this, SavedStylesActivity.class));
        });

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

        // 5. Bottom Navigation Logic
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.getMenu().findItem(R.id.nav_profile).setChecked(true);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // If you have a specific face shape stored, you'd pass it here
                Intent intent = new Intent(this, RecommendationsActivity.class);
                intent.putExtra("FACE_SHAPE", lastShape);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_saved) {
                // FIX: Navigate to SavedStylesActivity
                startActivity(new Intent(this, SavedStylesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true; // Already here
            }
            return false;
        });
    }
}