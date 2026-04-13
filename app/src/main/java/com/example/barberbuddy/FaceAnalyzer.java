package com.example.barberbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker.FaceLandmarkerOptions;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class FaceAnalyzer implements ImageAnalysis.Analyzer {

    public interface FaceShapeListener {
        void onFaceShapeDetected(FaceShapeAnalyzer.FaceShapeResult result,
                                 List<NormalizedLandmark> landmarks,
                                 int imageWidth,
                                 int imageHeight);
        void onNoFaceDetected();
    }

    private final FaceLandmarker faceLandmarker;
    private final FaceShapeListener listener;
    private int lastImageWidth  = 1;
    private int lastImageHeight = 1;

    public FaceAnalyzer(Context context, FaceShapeListener listener) {
        this.listener = listener;

        FaceLandmarkerOptions options = FaceLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder()
                        .setModelAssetPath("face_landmarker.task")
                        .build())
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.75f) // Increased from 0.5
                .setMinTrackingConfidence(0.75f)      // Increased from 0.5
                .setResultListener(this::handleResult)
                .build();

        faceLandmarker = FaceLandmarker.createFromOptions(context, options);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        if (rotationDegrees == 90 || rotationDegrees == 270) {
            lastImageWidth  = imageProxy.getHeight();
            lastImageHeight = imageProxy.getWidth();
        } else {
            lastImageWidth  = imageProxy.getWidth();
            lastImageHeight = imageProxy.getHeight();
        }

        Bitmap bitmap = imageProxyToBitmap(imageProxy, rotationDegrees);
        if (bitmap == null) { imageProxy.close(); return; }

        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        faceLandmarker.detectAsync(mpImage, imageProxy.getImageInfo().getTimestamp());
        imageProxy.close();
    }

    private void handleResult(FaceLandmarkerResult result, MPImage image) {
        if (result.faceLandmarks().isEmpty()) {
            listener.onNoFaceDetected();
            return;
        }
        List<NormalizedLandmark> landmarks = result.faceLandmarks().get(0);

        // Run fuzzy face shape analysis
        FaceShapeAnalyzer.FaceShapeResult shapeResult =
                FaceShapeAnalyzer.analyze(landmarks);

        listener.onFaceShapeDetected(shapeResult, landmarks,
                lastImageWidth, lastImageHeight);
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy, int rotationDegrees) {
        try {
            ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                    imageProxy.getWidth(), imageProxy.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(
                    new Rect(0, 0, imageProxy.getWidth(), imageProxy.getHeight()), 90, out);

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    out.toByteArray(), 0, out.toByteArray().length);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);
            return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void analyzeBitmap(android.graphics.Bitmap bitmap) {
        // MediaPipe Image can be created directly from a Bitmap
        com.google.mediapipe.framework.image.BitmapImageBuilder builder =
                new com.google.mediapipe.framework.image.BitmapImageBuilder(bitmap);
        com.google.mediapipe.framework.image.MPImage mpImage = builder.build();

        // Use the same detector instance you already have
        if (faceLandmarker != null) {
            faceLandmarker.detectAsync(mpImage, System.currentTimeMillis());
            // The results will come back through the existing resultListener
            // and trigger the onFaceShapeDetected callback in MainActivity.
        }
    }
}