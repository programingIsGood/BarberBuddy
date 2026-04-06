package com.example.barberbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

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

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private LandmarkOverlayView overlayView;
    private TextView tvFaceShape;
    private TextView tvInstruction;
    private FrameLayout btnCapture;      // Changed from Button to FrameLayout
    private ImageView ivUploadAction;   // New Upload text

    private ExecutorService cameraExecutor;
    private String currentFaceShape = "";
    private FaceShapeAnalyzer.FaceShapeResult lastResult;

    // Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleGalleryImage(uri);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) startCamera();
                else handlePermissionDenied();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize with new IDs from the matched XML
        previewView    = findViewById(R.id.previewView);
        overlayView    = findViewById(R.id.overlayView);
        tvFaceShape    = findViewById(R.id.tvFaceShape);
        tvInstruction  = findViewById(R.id.tvInstruction);
        btnCapture     = findViewById(R.id.btnCapture);
        ivUploadAction = findViewById(R.id.ivUploadAction);

        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 2. Navigation Logic (Triggered by the circular shutter button)
        btnCapture.setOnClickListener(v -> {
            if (!currentFaceShape.isEmpty() && lastResult != null) {
                navigateToResults();
            } else {
                Toast.makeText(this, "Please position your face in the oval", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Upload Logic
        ivUploadAction.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        checkCameraPermission();
    }

    private void navigateToResults() {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("FACE_SHAPE",      currentFaceShape);
        intent.putExtra("CONFIDENCE",      lastResult.confidence);
        intent.putExtra("SECONDARY_SHAPE", lastResult.getSecondaryShape());

        HashMap<String, Integer> scoreMap = new HashMap<>();
        for (FaceShapeAnalyzer.ShapeMembership sm : lastResult.allMemberships) {
            scoreMap.put(sm.shape, Math.round(sm.membership * 100));
        }
        intent.putExtra("FUZZY_SCORES", scoreMap);
        startActivity(intent);
    }

    private void handleGalleryImage(Uri uri) {
        // Logic to process an uploaded photo would go here.
        // For now, we can just show a toast.
        Toast.makeText(this, "Image selected! Processing...", Toast.LENGTH_SHORT).show();
    }

    // --- CAMERA LOGIC (Same as before, just updating UI hooks) ---

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                FaceAnalyzer faceAnalyzer = new FaceAnalyzer(this, new FaceAnalyzer.FaceShapeListener() {
                    @Override
                    public void onFaceShapeDetected(FaceShapeAnalyzer.FaceShapeResult result,
                                                    List<NormalizedLandmark> landmarks,
                                                    int imageWidth, int imageHeight) {
                        runOnUiThread(() -> {
                            lastResult = result;
                            currentFaceShape = result.primaryShape;

                            // UI Updates to match the new floating style
                            tvFaceShape.setVisibility(View.VISIBLE);
                            tvFaceShape.setText("Detected: " + result.primaryShape);
                            tvInstruction.setText("Looking good! Tap the button to see styles.");

                            // Visual feedback: Make the shutter button more prominent
                            btnCapture.setAlpha(1.0f);
                            overlayView.updateLandmarks(landmarks, imageWidth, imageHeight, true);
                        });
                    }

                    @Override
                    public void onNoFaceDetected() {
                        runOnUiThread(() -> {
                            lastResult = null;
                            currentFaceShape = "";
                            tvFaceShape.setVisibility(View.GONE);
                            tvInstruction.setText("Centre your face in the oval");
                            btnCapture.setAlpha(0.5f); // Dim button if no face
                            overlayView.clearLandmarks();
                        });
                    }
                });

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, faceAnalyzer);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis);

            } catch (Exception e) { e.printStackTrace(); }
        }, ContextCompat.getMainExecutor(this));
    }

    // --- PERMISSION HELPERS ---
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void handlePermissionDenied() {
        Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}