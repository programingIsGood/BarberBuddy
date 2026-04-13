package com.example.barberbuddy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFrontFacingFragment;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TryOnActivity extends AppCompatActivity {

    private boolean mUserRequestedInstall = true;
    private boolean isArCoreChecking = false;

    private static final String TAG = "TryOnActivity";

    private ArFrontFacingFragment arFragment;
    private ProgressBar loadingProgress;

    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();
    private final Set<CompletableFuture<?>> loaders = new HashSet<>();

    private ModelRenderable hairRenderable;
    private boolean modelLoaded = false;
    private Hairstyle pendingHairstyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        loadingProgress = findViewById(R.id.loadingProgress);
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

                            loadModel(pendingHairstyle);
                            setupSceneTracking();
                        }
                    }
                });
            }
        });
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

        runOnUiThread(() -> loadingProgress.setVisibility(View.VISIBLE));

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
                            runOnUiThread(() -> loadingProgress.setVisibility(View.GONE));
                        })
                        .exceptionally(throwable -> {
                            Log.e(TAG, "Model load failed: " + modelPath, throwable);
                            runOnUiThread(() -> {
                                loadingProgress.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "Could not load hair model. Check assets/models/ folder.",
                                        Toast.LENGTH_LONG).show();
                            });
                            return null;
                        })
        );
    }

    private void setupSceneTracking() {
        ArSceneView sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

        sceneView.getScene().addOnUpdateListener(frameTime -> {
            if (!modelLoaded || hairRenderable == null) return;
            if (sceneView.getSession() == null) return;

            try {
                java.util.Collection<AugmentedFace> faceList =
                        sceneView.getSession().getAllTrackables(AugmentedFace.class);

                for (AugmentedFace face : faceList) {
                    if (!faceNodeMap.containsKey(face)) {
                        AugmentedFaceNode faceNode = new AugmentedFaceNode(face);
                        faceNode.setParent(sceneView.getScene());

                        com.google.ar.sceneform.Node hairNode = new com.google.ar.sceneform.Node();
                        hairNode.setParent(faceNode);
                        hairNode.setRenderable(hairRenderable);

                        faceNodeMap.put(face, faceNode);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (CompletableFuture<?> loader : loaders) {
            if (!loader.isDone()) loader.cancel(true);
        }
    }
}
