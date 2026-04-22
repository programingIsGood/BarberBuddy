package com.example.barberbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/**
 * CameraPermissionActivity
 *
 * Shows a clear, friendly explanation of WHY camera access is needed BEFORE
 * the system permission dialog appears. Handles all three outcomes:
 *
 *   1. Granted            → proceeds to MainActivity (camera scan)
 *   2. Denied (soft)      → shows retry UI with explanation
 *   3. Permanently denied → guides user to app Settings
 */
public class CameraPermissionActivity extends AppCompatActivity {

    private View    layoutRationale, layoutDenied, layoutPermanentDenied;
    private boolean hasShownRationale = false;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            onPermissionGranted();
                        } else {
                            onPermissionDenied();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_permission);

        layoutRationale       = findViewById(R.id.layoutRationale);
        layoutDenied          = findViewById(R.id.layoutDenied);
        layoutPermanentDenied = findViewById(R.id.layoutPermanentDenied);

        // Check if we already have permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
            return;
        }

        // Show our rationale screen first
        showRationale();

        MaterialButton btnAllow  = findViewById(R.id.btnAllow);
        MaterialButton btnSkip   = findViewById(R.id.btnSkip);
        MaterialButton btnRetry  = findViewById(R.id.btnRetry);
        MaterialButton btnGoSettings = findViewById(R.id.btnGoSettings);

        if (btnAllow != null) btnAllow.setOnClickListener(v -> requestCameraPermission());
        if (btnSkip  != null) btnSkip.setOnClickListener(v -> proceedWithoutCamera());
        if (btnRetry != null) btnRetry.setOnClickListener(v -> requestCameraPermission());
        if (btnGoSettings != null) btnGoSettings.setOnClickListener(v -> openAppSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If user came back from Settings having granted permission
        if (hasShownRationale &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        }
    }

    // ── Permission actions ─────────────────────────────────────────────────────

    private void requestCameraPermission() {
        hasShownRationale = true;
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void onPermissionGranted() {
        Intent next = new Intent(this, MainActivity.class);
        next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(next);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void onPermissionDenied() {
        // Check if permanently denied (shouldShowRequestPermissionRationale returns false
        // after a permanent denial)
        boolean canAskAgain = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        if (canAskAgain) {
            showDenied();
        } else {
            showPermanentlyDenied();
        }
    }

    private void proceedWithoutCamera() {
        // Allow user to continue but inform them camera features won't work
        Intent next = new Intent(this, MainActivity.class);
        next.putExtra("CAMERA_UNAVAILABLE", true);
        next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(next);
        finish();
    }

    private void openAppSettings() {
        hasShownRationale = true;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    // ── Layout visibility ──────────────────────────────────────────────────────

    private void showRationale() {
        setVisible(layoutRationale, true);
        setVisible(layoutDenied, false);
        setVisible(layoutPermanentDenied, false);
    }

    private void showDenied() {
        setVisible(layoutRationale, false);
        setVisible(layoutDenied, true);
        setVisible(layoutPermanentDenied, false);
    }

    private void showPermanentlyDenied() {
        setVisible(layoutRationale, false);
        setVisible(layoutDenied, false);
        setVisible(layoutPermanentDenied, true);
    }

    private void setVisible(View view, boolean visible) {
        if (view != null) view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
