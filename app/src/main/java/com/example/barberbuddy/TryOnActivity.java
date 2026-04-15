package com.example.barberbuddy;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFrontFacingFragment;
import com.google.ar.sceneform.ux.AugmentedFaceNode;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TryOnActivity extends AppCompatActivity {

    private boolean mUserRequestedInstall = true;
    private boolean isArCoreChecking = false;

    private static final String TAG = "TryOnActivity";

    private ArFrontFacingFragment arFragment;
    private ProgressBar loadingProgress;
    private ImageView hairMaskOverlay;
    private TextView tvHint;
    private TextView tvSegmentationStatus;

    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();
    private final Set<CompletableFuture<?>> loaders = new HashSet<>();

    private ModelRenderable hairRenderable;
    private ModelRenderable occlusionRenderable;
    private boolean modelLoaded = false;
    private boolean occlusionMeshReady = false;
    private boolean segmentationEnabled = false;
    private boolean faceDetected = false;
    private Hairstyle pendingHairstyle;
    private CompletableFuture<com.google.ar.sceneform.rendering.Material> occlusionMaterialFuture;

    private HairSegmentationHelper hairSegmentationHelper;
    private Bitmap lastCameraFrame;
    private List<NormalizedLandmark> currentLandmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        loadingProgress = findViewById(R.id.loadingProgress);
        hairMaskOverlay = findViewById(R.id.hairMaskOverlay);
        tvHint = findViewById(R.id.tvHint);
        tvSegmentationStatus = findViewById(R.id.tvSegmentationStatus);
        findViewById(R.id.btnBackTryOn).setOnClickListener(v -> finish());

        int id = getIntent().getIntExtra("HAIRSTYLE_ID", -1);
        pendingHairstyle = HairstyleRepository.getById(id);

        if (pendingHairstyle == null) {
            Toast.makeText(this, "Hairstyle not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!Sceneform.isSupported(this)) {
            Toast.makeText(this, "AR not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.arFragmentContainer, ArFrontFacingFragment.class, null)
                    .commit();
        }

        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof ArFrontFacingFragment) {
                arFragment = (ArFrontFacingFragment) fragment;

                arFragment.getViewLifecycleOwnerLiveData().observe(this, viewLifecycleOwner -> {
                    if (viewLifecycleOwner != null) {
                        ArSceneView sceneView = arFragment.getArSceneView();

                        if (sceneView != null) {
                            try {
                                ArCoreApk.getInstance().requestInstall(this, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            initializeHairSegmentation();
                            createOcclusionMaterial();
                            loadModel(pendingHairstyle);
                            setupSceneTracking();
                        }
                    }
                });
            }
        });
    }

    private void initializeHairSegmentation() {
        hairSegmentationHelper = new HairSegmentationHelper();
        tvSegmentationStatus.setText("Initializing hair segmentation...");

        hairSegmentationHelper.initialize(this, success -> {
            runOnUiThread(() -> {
                if (success) {
                    segmentationEnabled = true;
                    tvSegmentationStatus.setText("Hair segmentation ready");
                    Log.d(TAG, "Hair segmentation initialized successfully");
                } else {
                    segmentationEnabled = false;
                    tvSegmentationStatus.setText("Segmentation unavailable, using fallback");
                    Toast.makeText(this,
                            "Hair segmentation unavailable. Using visual fallback.",
                            Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Hair segmentation failed to initialize");
                }
            });
        });
    }

    private void createOcclusionMaterial() {
        Color skinColor = new Color(1f, 0.878f, 0.698f);

        MaterialFactory.makeOpaqueWithColor(this, skinColor)
                .thenAccept(material -> {
                    occlusionMaterialFuture = CompletableFuture.completedFuture(material);
                    Log.d(TAG, "Occlusion material created");
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Failed to create occlusion material", throwable);
                    return null;
                });
    }

    private void createOcclusionMesh() {
        if (occlusionMaterialFuture == null || !occlusionMaterialFuture.isDone()) {
            Log.w(TAG, "Occlusion material not ready yet");
            return;
        }

        try {
            com.google.ar.sceneform.rendering.Material material = occlusionMaterialFuture.get();

            Renderable occlusionShape = ShapeFactory.makeCube(
                    new Vector3(0.32f, 0.08f, 0.20f),
                    Vector3.zero(),
                    material
            );

            occlusionRenderable = (ModelRenderable) occlusionShape;
            occlusionMeshReady = true;

            runOnUiThread(() -> {
                tvHint.setText("Face detected! Placing hairstyle...");
            });

            Log.d(TAG, "Occlusion mesh created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to create occlusion mesh", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isArCoreChecking) return;

        try {
            isArCoreChecking = true;
            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall);

            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                mUserRequestedInstall = false;
                isArCoreChecking = false;
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "ARCore installation exception", e);
        } finally {
            isArCoreChecking = false;
        }
    }

    private boolean modelFileExists(String assetPath) {
        try {
            getAssets().open(assetPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void loadModel(Hairstyle hairstyle) {
        String modelPath = hairstyle.getModelPath();

        if (!modelFileExists(modelPath)) {
            runOnUiThread(() -> {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(this,
                        "Error: No AR function available for this hairstyle. Wait for future updates.",
                        Toast.LENGTH_LONG).show();
            });
            return;
        }

        runOnUiThread(() -> {
            loadingProgress.setVisibility(View.VISIBLE);
            tvHint.setText("Loading hairstyle model...");
        });

        android.net.Uri modelUri = android.net.Uri.parse(
                "file:///android_asset/" + modelPath
        );

        loaders.add(
                ModelRenderable.builder()
                        .setSource(this, modelUri)
                        .setIsFilamentGltf(true)
                        .setRegistryId(modelPath)
                        .build()
                        .thenAccept(renderable -> {
                            hairRenderable = renderable;
                            hairRenderable.setShadowCaster(false);
                            hairRenderable.setShadowReceiver(false);

                            modelLoaded = true;

                            createOcclusionMesh();
                            runOnUiThread(() -> {
                                loadingProgress.setVisibility(View.GONE);
                                tvHint.setText("Point camera at your face");
                            });
                        })
                        .exceptionally(throwable -> {
                            Log.e(TAG, "Model load failed: " + modelPath, throwable);
                            runOnUiThread(() -> {
                                loadingProgress.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "Could not load hair model.",
                                        Toast.LENGTH_LONG).show();
                            });
                            return null;
                        })
        );
    }

    public void onCameraFrame(Bitmap frame, List<NormalizedLandmark> landmarks) {
        this.lastCameraFrame = frame;
        this.currentLandmarks = landmarks;

        if (segmentationEnabled && frame != null) {
            hairSegmentationHelper.processFrameAsync(frame, landmarks,
                    new HairSegmentationHelper.SegmentationCallback() {
                        @Override
                        public void onSegmentationResult(Bitmap hairMask, Bitmap coloredMask) {
                            if (coloredMask != null && !coloredMask.isRecycled()) {
                                runOnUiThread(() -> {
                                    hairMaskOverlay.setImageBitmap(coloredMask);
                                    hairMaskOverlay.setAlpha(0.85f);
                                });
                            }
                        }

                        @Override
                        public void onSegmentationError(String error) {
                            Log.w(TAG, "Segmentation error: " + error);
                        }
                    });
        }
    }

    public void onFaceDetected(List<NormalizedLandmark> landmarks) {
        this.currentLandmarks = landmarks;
        this.faceDetected = true;

        runOnUiThread(() -> {
            if (tvHint.getText().toString().contains("Point")) {
                tvHint.setText("Face detected! Processing...");
            }
        });
    }

    public void onFaceLost() {
        this.faceDetected = false;

        runOnUiThread(() -> {
            hairMaskOverlay.setAlpha(0f);
            tvHint.setText("Face lost - Point camera at your face");
        });
    }

    private void setupSceneTracking() {
        ArSceneView sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

        sceneView.getScene().addOnUpdateListener(frameTime -> {
            if (sceneView.getSession() == null) return;

            if (!occlusionMeshReady && occlusionMaterialFuture != null && occlusionMaterialFuture.isDone()) {
                createOcclusionMesh();
            }

            try {
                java.util.Collection<AugmentedFace> faceList =
                        sceneView.getSession().getAllTrackables(AugmentedFace.class);

                boolean hasFace = !faceList.isEmpty();

                if (hasFace && !faceDetected) {
                    onFaceDetected(null);
                } else if (!hasFace && faceDetected) {
                    onFaceLost();
                }

                for (AugmentedFace face : faceList) {
                    if (!faceNodeMap.containsKey(face)) {
                        if (modelLoaded && occlusionMeshReady) {
                            attachMeshesToFace(face);
                        }
                    }
                }

                faceNodeMap.entrySet().removeIf(entry -> {
                    if (entry.getKey().getTrackingState() == TrackingState.STOPPED) {
                        entry.getValue().setParent(null);
                        return true;
                    }
                    return false;
                });

            } catch (Exception e) {
                Log.e(TAG, "Face tracking error", e);
            }
        });
    }

    private void attachMeshesToFace(AugmentedFace face) {
        if (faceNodeMap.containsKey(face)) return;

        AugmentedFaceNode faceNode = new AugmentedFaceNode(face);
        faceNode.setParent(arFragment.getArSceneView().getScene());

        if (occlusionRenderable != null) {
            Node occlusionNode = new Node();
            occlusionNode.setParent(faceNode);
            occlusionNode.setRenderable(occlusionRenderable);

            com.google.ar.core.Pose facePose = face.getCenterPose();
            float[] translation = facePose.getTranslation();
            occlusionNode.setWorldPosition(new Vector3(
                    translation[0],
                    translation[1] + 0.10f,
                    translation[2] - 0.015f
            ));

            runOnUiThread(() -> {
                if (tvHint.getText().toString().contains("Processing")) {
                    tvHint.setText("Hair covered! Try the hairstyle");
                }
            });
        }

        if (hairRenderable != null) {
            Node hairNode = new Node();
            hairNode.setParent(faceNode);
            hairNode.setRenderable(hairRenderable);
        }

        faceNodeMap.put(face, faceNode);

        Log.d(TAG, "Attached occlusion mesh and hair model to face");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (CompletableFuture<?> loader : loaders) {
            if (!loader.isDone()) loader.cancel(true);
        }

        if (hairSegmentationHelper != null) {
            hairSegmentationHelper.release();
        }
    }
}
