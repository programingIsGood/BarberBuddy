package com.example.barberbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class TryOnOverlayView extends View {
    private Bitmap hairBitmap;
    private List<NormalizedLandmark> landmarks;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public TryOnOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHairAsset(int resId) {
        hairBitmap = BitmapFactory.decodeResource(getResources(), resId);
        invalidate();
    }

    public void updateFrame(List<NormalizedLandmark> landmarks, int width, int height) {
        this.landmarks = landmarks;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (landmarks == null || hairBitmap == null || landmarks.isEmpty()) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        // 1. Define the Mesh Grid (4x4 points)
        int meshWidth = 3;
        int meshHeight = 3;
        int count = (meshWidth + 1) * (meshHeight + 1);
        float[] verts = new float[count * 2]; // Stores x,y coordinates for the mesh

        // 2. Identify Anchor Landmarks
        // 10: Forehead Top, 234: Right Temple, 454: Left Temple, 152: Chin, 151: Forehead Center
        NormalizedLandmark forehead = landmarks.get(10);
        NormalizedLandmark foreheadCenter = landmarks.get(151);
        NormalizedLandmark LTemple = landmarks.get(454);
        NormalizedLandmark RTemple = landmarks.get(234);
        NormalizedLandmark chin = landmarks.get(152);

        // Calculate face scale
        float headWidth = Math.abs(LTemple.x() - RTemple.x()) * viewWidth;
        float headHeight = Math.abs(chin.y() - forehead.y()) * viewHeight;

        // 3. GENERATE THE WARPED VERTICES
        // We map the 16 points of our 4x4 grid to the face area
        int index = 0;
        for (int y = 0; y <= meshHeight; y++) {
            // Vertical ratio (0.0 at top of hair, 1.0 at hairline)
            float fy = (float) y / meshHeight;

            for (int x = 0; x <= meshWidth; x++) {
                // Horizontal ratio (0.0 at left side, 1.0 at right side)
                float fx = (float) x / meshWidth;

                // Target X: Stretch between temples, using forehead center as anchor
                float centerX = foreheadCenter.x() * viewWidth;
                float targetX = centerX + (fx - 0.5f) * (headWidth * 1.5f);

                // Target Y: The bottom of the mesh (y=meshHeight) sits on the hairline
                // The top of the mesh (y=0) is pushed up based on head height
                float hairlineY = forehead.y() * viewHeight;
                float targetY = hairlineY - ((1.0f - fy) * (headHeight * 0.5f));

                // Apply a slight "Bulge" effect for 3D realism
                // We push the center points outward more than the edges
                if (x > 0 && x < meshWidth) {
                    float bulge = (float) Math.sin(fx * Math.PI) * (headWidth * 0.1f);
                    // If nose is turned (yaw), shift targetX
                    float noseX = landmarks.get(1).x() * viewWidth;
                    targetX += (noseX - centerX) * fy;
                }

                verts[index * 2] = targetX;
                verts[index * 2 + 1] = targetY;
                index++;
            }
        }

        // 4. RENDERING WITH MIRRORING
        canvas.save();
        // Front camera mirroring
        canvas.scale(-1, 1, viewWidth / 2f, viewHeight / 2f);

        // This is the magic function that stretches the image to fit the verts
        canvas.drawBitmapMesh(hairBitmap, meshWidth, meshHeight, verts, 0, null, 0, null);

        canvas.restore();
    }
}