package com.example.barberbuddy;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ARPreviewActivity extends AppCompatActivity {

    private PreviewView arPreviewView;
    private LandmarkOverlayView arLandmarkOverlay;
    private TextView tvStyleLabel;
    private LinearLayout llStyleSwitcher;

    private ExecutorService cameraExecutor;
    private int currentHairstyleId;
    private String currentHairstyleName;

    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startARCamera();
                else { Toast.makeText(this, "Camera needed for AR preview", Toast.LENGTH_LONG).show(); finish(); }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_preview);

        currentHairstyleId   = getIntent().getIntExtra("HAIRSTYLE_ID", 1);
        currentHairstyleName = getIntent().getStringExtra("HAIRSTYLE_NAME");
        if (currentHairstyleName == null) currentHairstyleName = "Style Preview";

        arPreviewView    = findViewById(R.id.arPreviewView);
        arLandmarkOverlay= findViewById(R.id.arLandmarkOverlay);
        tvStyleLabel     = findViewById(R.id.tvStyleLabel);
        llStyleSwitcher  = findViewById(R.id.llStyleSwitcher);

        tvStyleLabel.setText(currentHairstyleName + " - Overlay");

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Close button
        ImageButton btnClose = findViewById(R.id.btnCloseAR);
        btnClose.setOnClickListener(v -> finish());

        // Capture button
        findViewById(R.id.btnCapture).setOnClickListener(v -> captureScreen());

        // Build style switcher thumbnails
        buildStyleSwitcher();

        // Camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startARCamera();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void buildStyleSwitcher() {
        llStyleSwitcher.removeAllViews();
        List<Hairstyle> styles = HairstyleRepository.getAll();

        for (Hairstyle h : styles) {
            // Build small colored circle button for each style
            View thumb = new View(this);
            int size = (int) (56 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginStart((int)(8 * getResources().getDisplayMetrics().density));
            lp.setMarginEnd((int)(8 * getResources().getDisplayMetrics().density));
            thumb.setLayoutParams(lp);

            // Color based on position
            int[] colors = {0xFF2D4A2D, 0xFF4A2D2D, 0xFF2D2D4A, 0xFF4A4A2D};
            thumb.setBackgroundColor(colors[h.getId() % colors.length]);

            // Highlight current
            if (h.getId() == currentHairstyleId) {
                thumb.setAlpha(1.0f);
                thumb.setScaleX(1.2f);
                thumb.setScaleY(1.2f);
            } else {
                thumb.setAlpha(0.6f);
            }

            thumb.setOnClickListener(v -> {
                currentHairstyleId   = h.getId();
                currentHairstyleName = h.getName();
                tvStyleLabel.setText(currentHairstyleName + " - Overlay");
                buildStyleSwitcher(); // rebuild to highlight selected
            });

            llStyleSwitcher.addView(thumb);
        }
    }

    private void startARCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(this).get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(arPreviewView.getSurfaceProvider());

                // Lightweight face tracker for AR overlay positioning
                FaceAnalyzer analyzer = new FaceAnalyzer(this, new FaceAnalyzer.FaceShapeListener() {
                    @Override
                    public void onFaceShapeDetected(FaceShapeAnalyzer.FaceShapeResult result,
                                                    List<NormalizedLandmark> landmarks,
                                                    int w, int h) {
                        runOnUiThread(() ->
                                arLandmarkOverlay.updateLandmarks(landmarks, w, h, true));
                    }
                    @Override public void onNoFaceDetected() {
                        runOnUiThread(() -> arLandmarkOverlay.clearLandmarks());
                    }
                });

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(cameraExecutor, analyzer);

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureScreen() {
        // Capture the current view as a bitmap and save to gallery
        try {
            View root = getWindow().getDecorView().getRootView();
            root.setDrawingCacheEnabled(true);
            Bitmap screenshot = Bitmap.createBitmap(root.getDrawingCache());
            root.setDrawingCacheEnabled(false);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "BarberBuddy_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BarberBuddy");

            android.net.Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                if (out != null) {
                    screenshot.compress(Bitmap.CompressFormat.JPEG, 92, out);
                    out.close();
                }
            }
            Toast.makeText(this, "Saved to gallery! 📸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Capture failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
