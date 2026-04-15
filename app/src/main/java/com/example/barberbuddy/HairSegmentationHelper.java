package com.example.barberbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.ByteBufferExtractor;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter;
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HairSegmentationHelper {

    private static final String TAG = "HairSegmentationHelper";
    private static final String MODEL_PATH = "models/hair_segmenter.tflite";
    private static final int MODEL_INPUT_SIZE = 512;
    private static final int HAIR_CATEGORY = 1;

    private ImageSegmenter segmenter;
    private ExecutorService executor;
    private boolean isInitialized = false;
    private long lastTimestamp = 0;

    private int frameCount = 0;
    private static final int FRAME_SKIP = 3;

    private int skinColor = Color.rgb(255, 224, 178);

    public interface SegmentationCallback {
        void onSegmentationResult(Bitmap hairMask, Bitmap coloredMask);
        void onSegmentationError(String error);
    }

    public interface InitCallback {
        void onInitComplete(boolean success);
    }

    public HairSegmentationHelper() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void initialize(Context context, InitCallback callback) {
        executor.execute(() -> {
            try {
                ImageSegmenter.ImageSegmenterOptions options = ImageSegmenter.ImageSegmenterOptions.builder()
                        .setBaseOptions(BaseOptions.builder()
                                .setModelAssetPath(MODEL_PATH)
                                .build())
                        .setRunningMode(RunningMode.VIDEO)
                        .setOutputCategoryMask(true)
                        .setOutputConfidenceMasks(false)
                        .build();

                segmenter = ImageSegmenter.createFromOptions(context, options);
                isInitialized = true;
                Log.d(TAG, "HairSegmenter initialized successfully");

                if (callback != null) {
                    callback.onInitComplete(true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize HairSegmenter: " + e.getMessage());
                isInitialized = false;
                if (callback != null) {
                    callback.onInitComplete(false);
                }
            }
        });
    }

    public void setSkinColor(int color) {
        this.skinColor = color;
    }

    public void processFrameAsync(Bitmap frame, List<NormalizedLandmark> landmarks,
                                  SegmentationCallback callback) {
        if (!isInitialized || segmenter == null) {
            if (callback != null) {
                callback.onSegmentationError("Segmenter not initialized");
            }
            return;
        }

        frameCount++;
        if (frameCount % FRAME_SKIP != 0) {
            return;
        }

        executor.execute(() -> {
            try {
                long timestamp = System.currentTimeMillis() * 1000;

                Bitmap resizedFrame = Bitmap.createScaledBitmap(frame, MODEL_INPUT_SIZE,
                        MODEL_INPUT_SIZE, true);

                MPImage mpImage = new BitmapImageBuilder(resizedFrame).build();

                ImageSegmenterResult result = segmenter.segmentForVideo(mpImage, timestamp);

                Bitmap hairMask = createHairMaskBitmap(result, MODEL_INPUT_SIZE,
                        MODEL_INPUT_SIZE);

                Bitmap softMask = applyGaussianBlur(hairMask, 20f);

                Bitmap coloredMask = applySkinColorOverlay(softMask, skinColor);

                Bitmap fullSizeMask = Bitmap.createScaledBitmap(coloredMask,
                        frame.getWidth(), frame.getHeight(), true);

                if (callback != null) {
                    callback.onSegmentationResult(fullSizeMask, fullSizeMask);
                }

                resizedFrame.recycle();

                lastTimestamp = timestamp;

            } catch (Exception e) {
                Log.e(TAG, "Segmentation error: " + e.getMessage());
                if (callback != null) {
                    callback.onSegmentationError(e.getMessage());
                }
            }
        });
    }

    private Bitmap createHairMaskBitmap(ImageSegmenterResult result, int width, int height) {
        Bitmap maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if (result.categoryMask().isPresent()) {
            ByteBuffer buffer = ByteBufferExtractor.extract(result.categoryMask().get());

            int[] pixels = new int[width * height];
            buffer.rewind();

            for (int i = 0; i < pixels.length; i++) {
                if (buffer.remaining() > 0) {
                    int category = buffer.get() & 0xFF;

                    if (category == HAIR_CATEGORY) {
                        pixels[i] = Color.WHITE;
                    } else {
                        pixels[i] = Color.TRANSPARENT;
                    }
                }
            }

            maskBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        }

        return maskBitmap;
    }

    private Bitmap applyGaussianBlur(Bitmap source, float radius) {
        if (source == null || source.isRecycled()) {
            return null;
        }
        return applySimpleAlphaFade(source);
    }

    private Bitmap applySimpleAlphaFade(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int fadeRadius = 30;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixel = pixels[index];

                if (Color.alpha(pixel) > 0) {
                    int distFromEdge = calculateEdgeDistance(pixels, x, y, width, height);

                    if (distFromEdge < fadeRadius) {
                        float fade = (float) distFromEdge / fadeRadius;
                        int alpha = (int) (255 * fade * fade);
                        pixels[index] = Color.argb(alpha, 255, 224, 178);
                    }
                }
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private int calculateEdgeDistance(int[] pixels, int x, int y, int width, int height) {
        int distance = 0;
        int maxDist = Math.min(width, height) / 4;

        for (int d = 0; d < maxDist; d++) {
            int[] checkX = {x - d, x + d, x, x};
            int[] checkY = {y, y, y - d, y + d};

            for (int i = 0; i < 4; i++) {
                int cx = checkX[i];
                int cy = checkY[i];
                if (cx < 0 || cx >= width || cy < 0 || cy >= height) {
                    return d;
                }
                if (Color.alpha(pixels[cy * width + cx]) == 0) {
                    return d;
                }
            }
        }

        return maxDist;
    }

    private Bitmap applySkinColorOverlay(Bitmap mask, int color) {
        if (mask == null || mask.isRecycled()) {
            return null;
        }

        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(mask, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(230);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        canvas.drawRect(0, 0, result.getWidth(), result.getHeight(), paint);

        return result;
    }

    public void release() {
        if (segmenter != null) {
            segmenter.close();
            segmenter = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        isInitialized = false;
        Log.d(TAG, "HairSegmentationHelper released");
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
