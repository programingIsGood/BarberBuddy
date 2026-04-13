package com.example.barberbuddy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.ArrayList;
import java.util.List;

public class LandmarkOverlayView extends View {

    private final Paint dotPaint = new Paint();
    private final Paint outlinePaint = new Paint();

    private List<NormalizedLandmark> landmarks = new ArrayList<>();

    // Actual image dimensions from the analysis frame
    private int imageWidth = 1;
    private int imageHeight = 1;
    private boolean isFrontCamera = true;

    // Face oval landmark indices (MediaPipe 478-point model)
    private static final int[] OVAL_INDICES = {
            10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361,
            288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149,
            150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109
    };

    public LandmarkOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        dotPaint.setColor(Color.parseColor("#00E5FF"));
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
        dotPaint.setStrokeWidth(2f);

        outlinePaint.setColor(Color.parseColor("#8000E5FF"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3f);
        outlinePaint.setAntiAlias(true);
    }

    /**
     * Call this every time new landmarks arrive.
     * Pass the image width/height from the ImageProxy.
     */
    public void updateLandmarks(List<NormalizedLandmark> landmarks,
                                int imageWidth, int imageHeight,
                                boolean isFrontCamera) {
        this.landmarks = landmarks;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.isFrontCamera = isFrontCamera;
        postInvalidate();
    }

    public void clearLandmarks() {
        this.landmarks = new ArrayList<>();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (landmarks.isEmpty()) return;

        int viewWidth  = getWidth();
        int viewHeight = getHeight();

        // Step 1: figure out how the image maps onto the PreviewView
        // CameraX uses FILL_CENTER by default (like centerCrop but keeps aspect)
        float scaleX = (float) viewWidth  / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;

        // Use the larger scale so the image fills the view (same as centerCrop)
        float scale = Math.max(scaleX, scaleY);

        // Offset to center the scaled image inside the view
        float offsetX = (viewWidth  - imageWidth  * scale) / 2f;
        float offsetY = (viewHeight - imageHeight * scale) / 2f;

        // Draw face oval outline
        Path ovalPath = new Path();
        boolean first = true;

        for (int idx : OVAL_INDICES) {
            if (idx >= landmarks.size()) continue;
            PointF pt = landmarkToViewPoint(
                    landmarks.get(idx), scale, offsetX, offsetY, viewWidth);
            if (first) { ovalPath.moveTo(pt.x, pt.y); first = false; }
            else ovalPath.lineTo(pt.x, pt.y);
        }
        ovalPath.close();
        canvas.drawPath(ovalPath, outlinePaint);

        // Draw dots on every landmark
        for (NormalizedLandmark lm : landmarks) {
            PointF pt = landmarkToViewPoint(lm, scale, offsetX, offsetY, viewWidth);
            canvas.drawCircle(pt.x, pt.y, 2.5f, dotPaint);
        }
    }

    /**
     * Converts a normalized landmark [0..1] into actual view pixel coordinates,
     * accounting for scale, centering offset, and front camera mirroring.
     */
    private PointF landmarkToViewPoint(NormalizedLandmark lm,
                                       float scale, float offsetX, float offsetY,
                                       int viewWidth) {
        // Scale normalized coords to image pixel space
        float x = lm.x() * imageWidth  * scale + offsetX;
        float y = lm.y() * imageHeight * scale + offsetY;

        // Mirror X for front camera
        if (isFrontCamera) {
            x = viewWidth - x;
        }

        return new PointF(x, y);
    }
}