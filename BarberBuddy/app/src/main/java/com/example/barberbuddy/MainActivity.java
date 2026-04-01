package com.example.barberbuddy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private LandmarkOverlayView overlayView;
    private TextView tvFaceShape;
    private TextView tvInstruction;
    private Button btnGetRecommendations;

    private ExecutorService cameraExecutor;
    private String currentFaceShape = "";

    private FaceShapeAnalyzer.FaceShapeResult lastResult; // Store the full result here


    // Modern permission launcher — replaces onRequestPermissionsResult
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            // Check if user clicked "Don't ask again"
                            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                showGoToSettingsDialog();
                            } else {
                                Toast.makeText(this,
                                        "Camera permission is required to detect your face shape.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView           = findViewById(R.id.previewView);
        overlayView           = findViewById(R.id.overlayView);
        tvFaceShape           = findViewById(R.id.tvFaceShape);
        tvInstruction         = findViewById(R.id.tvInstruction);
        btnGetRecommendations = findViewById(R.id.btnGetRecommendations);

        // Must match scale type used in LandmarkOverlayView
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Inside MainActivity.java - btnGetRecommendations.setOnClickListener
        btnGetRecommendations.setOnClickListener(v -> {
            if (!currentFaceShape.isEmpty() && lastResult != null) {
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra("FACE_SHAPE",      currentFaceShape);
                intent.putExtra("CONFIDENCE",      lastResult.confidence);
                intent.putExtra("SECONDARY_SHAPE", lastResult.getSecondaryShape());

                // Convert memberships to a HashMap to pass through Intent
                HashMap<String, Integer> scoreMap = new HashMap<>();
                for (FaceShapeAnalyzer.ShapeMembership sm : lastResult.allMemberships) {
                    scoreMap.put(sm.shape, Math.round(sm.membership * 100));
                }
                intent.putExtra("FUZZY_SCORES", scoreMap);

                startActivity(intent);
            }
        });

        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Already granted — go straight to camera
            startCamera();

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // User denied once before — explain why we need it, then ask again
            new AlertDialog.Builder(this)
                    .setTitle("Camera Permission Needed")
                    .setMessage("BarberBuddy needs camera access to scan your face shape and recommend the best hairstyles for you.")
                    .setPositiveButton("Grant Permission", (dialog, which) ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        Toast.makeText(this,
                                "Camera permission is required to use BarberBuddy.",
                                Toast.LENGTH_LONG).show();
                    })
                    .show();

        } else {
            // First time asking — just launch the system dialog
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showGoToSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Camera permission was permanently denied. Please enable it in Settings to use BarberBuddy.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.fromParts(
                            "package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                FaceAnalyzer faceAnalyzer = new FaceAnalyzer(this,
                        new FaceAnalyzer.FaceShapeListener() {

                            @Override
                            public void onFaceShapeDetected(FaceShapeAnalyzer.FaceShapeResult result,
                                                            List<NormalizedLandmark> landmarks,
                                                            int imageWidth, int imageHeight) {
                                runOnUiThread(() -> {
                                    // Update our stored result so the button has the latest data
                                    lastResult = result;
                                    currentFaceShape = result.primaryShape;

                                    tvFaceShape.setVisibility(View.VISIBLE);
                                    tvFaceShape.setText(result.primaryShape);

                                    String secondary = result.getSecondaryShape();
                                    if (secondary != null) {
                                        tvInstruction.setText("Closest match: " + result.primaryShape
                                                + " with " + secondary + " features");
                                    } else {
                                        tvInstruction.setText("Looking good! Tap the button below.");
                                    }

                                    btnGetRecommendations.setEnabled(true);
                                    overlayView.updateLandmarks(landmarks, imageWidth, imageHeight, true);
                                });
                            }

                            @Override
                            public void onNoFaceDetected() {
                                runOnUiThread(() -> {
                                    lastResult = null; // Add this line
                                    currentFaceShape = ""; // Add this line
                                    tvFaceShape.setVisibility(View.INVISIBLE);
                                    tvInstruction.setText("Position your face in the frame");
                                    overlayView.clearLandmarks();
                                    btnGetRecommendations.setEnabled(false); // Disable button if no face
                                });
                            }
                        });

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, faceAnalyzer);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this,
                        "Failed to start camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
