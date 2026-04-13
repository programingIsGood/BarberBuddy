package com.example.barberbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private LandmarkOverlayView overlayView;
    private TextView tvFaceShape;
    private TextView tvInstruction;
    private FrameLayout btnCapture;
    private ImageView ivUploadAction;

    private ExecutorService cameraExecutor;
    private String currentFaceShape = "";
    private FaceShapeAnalyzer.FaceShapeResult lastResult;

    private long lastUpdateMs = 0;
    private static final long UPDATE_INTERVAL_MS = 300;

    // Camera permission
    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else showPermissionDenied();
            });

    // Gallery
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) processPhoto(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView    = findViewById(R.id.previewView);
        overlayView    = findViewById(R.id.overlayView);
        tvFaceShape    = findViewById(R.id.tvFaceShape);
        tvInstruction  = findViewById(R.id.tvInstruction);
        btnCapture     = findViewById(R.id.btnCapture);
        ivUploadAction = findViewById(R.id.ivUploadAction);

        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Capture button → go to results
        btnCapture.setOnClickListener(v -> {
            if (lastResult == null) {
                Toast.makeText(this, "No face detected", Toast.LENGTH_SHORT).show();
                return;
            }
            navigateToResults();
        });

        // Upload image
        ivUploadAction.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        checkCameraPermission();
    }

    // ─────────────────────────────
    // CAMERA
    // ─────────────────────────────
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                FaceAnalyzer analyzer = new FaceAnalyzer(this,
                        new FaceAnalyzer.FaceShapeListener() {

                            @Override
                            public void onFaceShapeDetected(
                                    FaceShapeAnalyzer.FaceShapeResult result,
                                    List<NormalizedLandmark> landmarks,
                                    int w, int h) {

                                long now = System.currentTimeMillis();
                                if (now - lastUpdateMs < UPDATE_INTERVAL_MS) return;
                                lastUpdateMs = now;

                                runOnUiThread(() -> {
                                    lastResult = result;
                                    currentFaceShape = result.primaryShape;

                                    tvFaceShape.setVisibility(View.VISIBLE);
                                    tvFaceShape.setText("Detected: " + result.primaryShape);

                                    tvInstruction.setText("Tap the button to continue");

                                    overlayView.updateLandmarks(landmarks, w, h, true);
                                });
                            }

                            @Override
                            public void onNoFaceDetected() {
                                runOnUiThread(() -> {
                                    lastResult = null;
                                    currentFaceShape = "";

                                    tvFaceShape.setVisibility(View.GONE);
                                    tvInstruction.setText("Centre your face in the oval");

                                    overlayView.clearLandmarks();
                                });
                            }
                        });

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, analyzer);

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysis
                );

            } catch (Exception e) {
                Toast.makeText(this, "Camera failed", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ─────────────────────────────
    // GALLERY PROCESSING
    // ─────────────────────────────
    private void processPhoto(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media
                    .getBitmap(getContentResolver(), uri);

            FaceAnalyzer analyzer = new FaceAnalyzer(this,
                    new FaceAnalyzer.FaceShapeListener() {

                        @Override
                        public void onFaceShapeDetected(
                                FaceShapeAnalyzer.FaceShapeResult result,
                                List<NormalizedLandmark> landmarks,
                                int w, int h) {

                            runOnUiThread(() -> {
                                lastResult = result;
                                currentFaceShape = result.primaryShape;
                                navigateToResults();
                            });
                        }

                        @Override
                        public void onNoFaceDetected() {
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this,
                                            "No face detected in image",
                                            Toast.LENGTH_LONG).show());
                        }
                    });

            analyzer.analyzeBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(this, "Image failed", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────
    // NAVIGATION
    // ─────────────────────────────
    private void navigateToResults() {
        HashMap<String, Integer> scoreMap = new HashMap<>();

        for (FaceShapeAnalyzer.ShapeMembership sm : lastResult.allMemberships) {
            scoreMap.put(sm.shape, Math.round(sm.membership * 100));
        }

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("FACE_SHAPE", currentFaceShape);
        intent.putExtra("CONFIDENCE", lastResult.confidence);
        intent.putExtra("SECONDARY_SHAPE", lastResult.getSecondaryShape());
        intent.putExtra("FUZZY_SCORES", scoreMap);

        startActivity(intent);
    }

    // ─────────────────────────────
    // PERMISSION
    // ─────────────────────────────
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showPermissionDenied() {
        new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("Camera is required.")
                .setPositiveButton("Settings", (d, w) -> {
                    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(i);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}