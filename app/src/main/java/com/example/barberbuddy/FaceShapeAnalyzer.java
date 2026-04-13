package com.example.barberbuddy;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes face shape using anthropometric ratios and fuzzy logic membership functions.
 *
 * Ratios used:
 *   R1 = Cheekbone Width / Face Height        → overall face width-to-height
 *   R2 = Jaw Width / Cheekbone Width          → how wide the jaw is vs cheeks
 *   R3 = Forehead Width / Cheekbone Width     → how wide forehead is vs cheeks
 *   R4 = Jaw Width / Forehead Width           → jaw vs forehead symmetry
 *   R5 = Face Height / Cheekbone Width        → elongation ratio
 *
 * Fuzzy logic assigns a membership degree (0.0–1.0) to each face shape,
 * then picks the shape with the highest membership as the primary result
 * while keeping secondary matches for nuanced recommendations.
 */
public class FaceShapeAnalyzer {
    // Cheekbones (Direct distance 50 to 280)
    private static final int RIGHT_CHEEK = 50;
    private static final int LEFT_CHEEK  = 280;

    // Jaw (Symmetry: 152 to 288 * 2)
    private static final int CHIN     = 152;
    private static final int JAW_EDGE = 288;

    // Forehead (Direct distance 103 to 332)
    private static final int RIGHT_FOREHEAD = 103;
    private static final int LEFT_FOREHEAD  = 332;

    // Height (Direct distance 10 to 52)
    private static final int FACE_TOP    = 10;
    private static final int FACE_BOTTOM = 152;

    private static final int LEFT_JAW_LOWER = 58;

    // ─── Result class ─────────────────────────────────────────────────────────

    public static class FaceShapeResult {

        // Primary detected shape (highest fuzzy membership)
        public final String primaryShape;

        // Confidence 0–100 for the primary shape
        public final int confidence;

        // All shape memberships sorted by score
        public final List<ShapeMembership> allMemberships;

        // Raw ratios for debugging / display
        public final FaceRatios ratios;

        // Human-readable explanation
        public final String explanation;

        public FaceShapeResult(String primaryShape, int confidence,
                               List<ShapeMembership> allMemberships,
                               FaceRatios ratios, String explanation) {
            this.primaryShape   = primaryShape;
            this.confidence     = confidence;
            this.allMemberships = allMemberships;
            this.ratios         = ratios;
            this.explanation    = explanation;
        }

        /**
         * Returns the secondary shape if its membership is at least 30%
         * of the primary — useful for "you're between X and Y" messaging.
         */
        public String getSecondaryShape() {
            if (allMemberships.size() < 2) return null;
            ShapeMembership top    = allMemberships.get(0);
            ShapeMembership second = allMemberships.get(1);

            // If the second choice has at least 0.30 membership, recommend it too
            if (second.membership >= 0.30f) {
                return second.shape;
            }
            return null;
        }
    }


    public static class ShapeMembership {
        public final String shape;
        public final float membership;   // 0.0 – 1.0

        public ShapeMembership(String shape, float membership) {
            this.shape      = shape;
            this.membership = membership;
        }
    }

    public static class FaceRatios {
        public float cheekboneWidth;
        public float jawWidth;
        public float foreheadWidth;
        public float faceHeight;
        public float r1_widthToHeight;   // cheek / height
        public float r2_jawToCheek;      // jaw / cheek
        public float r3_foreheadToCheek; // forehead / cheek
        public float r4_jawToForehead;   // jaw / forehead
        public float r5_elongation;      // height / cheek
        public float jawCurve;           // how pointed/curved the jaw is
    }

    // ─── Main entry point ─────────────────────────────────────────────────────

    public static FaceShapeResult analyze(List<NormalizedLandmark> landmarks) {
        FaceRatios ratios = computeRatios(landmarks);
        Map<String, Float> memberships = computeFuzzyMemberships(ratios);
        return buildResult(memberships, ratios);
    }

    // ─── Step 1: Compute anthropometric ratios ────────────────────────────────

    private static FaceRatios computeRatios(List<NormalizedLandmark> lm) {
        FaceRatios r = new FaceRatios();

        // 1. Jaw Width (Distance 152 to 288 multiplied by 2)
        r.jawWidth = dist(lm.get(CHIN), lm.get(JAW_EDGE)) * 2.0f;

        // 2. Cheekbone Width (Direct distance 50 to 280)
        r.cheekboneWidth = dist(lm.get(RIGHT_CHEEK), lm.get(LEFT_CHEEK));

        // 3. Forehead Width (Direct distance 103 to 332)
        r.foreheadWidth = dist(lm.get(RIGHT_FOREHEAD), lm.get(LEFT_FOREHEAD));

        // 4. Face Height (Direct distance 10 to 52)
        r.faceHeight = dist(lm.get(FACE_TOP), lm.get(FACE_BOTTOM));

        // 5. Jaw Curve Detection
        // Note: Using your new jawWidth calculation as the base
        float lowerJawW = dist(lm.get(CHIN), lm.get(LEFT_JAW_LOWER)) * 2.0f;
        r.jawCurve = (r.jawWidth > 0) ? lowerJawW / r.jawWidth : 0.5f;

        // 6. Final Ratio Calculations (R1 - R5)
        float cheek = Math.max(r.cheekboneWidth, 0.0001f);
        float height = Math.max(r.faceHeight, 0.0001f);
        float forehead = Math.max(r.foreheadWidth, 0.0001f);

        r.r1_widthToHeight   = r.cheekboneWidth / height;
        r.r2_jawToCheek      = r.jawWidth / cheek;
        r.r3_foreheadToCheek = r.foreheadWidth / cheek;
        r.r4_jawToForehead   = r.jawWidth / forehead;
        r.r5_elongation      = height / cheek;

        return r;
    }

    // ─── Step 2: Fuzzy membership functions ──────────────────────────────────
    //
    // Each shape has a membership function that takes the ratios and returns
    // a value in [0, 1] representing how well the face fits that shape.
    // We use trapezoidal and triangular membership functions — standard in
    // fuzzy logic systems for continuous measurements.
    //
    // trapezoid(x, a, b, c, d):
    //   0 if x < a
    //   rises linearly from 0→1 between a and b
    //   1 between b and c (the flat top / core region)
    //   falls linearly from 1→0 between c and d
    //   0 if x > d
    //
    // triangle(x, a, b, c):
    //   0 if x < a
    //   rises linearly from 0→1 between a and b (peak)
    //   falls linearly from 1→0 between b and c
    //   0 if x > c

    private static Map<String, Float> computeFuzzyMemberships(FaceRatios r) {
        Map<String, Float> m = new HashMap<>();

        m.put("Oval",    membershipOval(r));
        m.put("Round",   membershipRound(r));
        m.put("Square",  membershipSquare(r));
        m.put("Heart",   membershipHeart(r));
        m.put("Oblong",  membershipOblong(r));
        m.put("Diamond", membershipDiamond(r));
        m.put("Triangle", membershipTriangle(r));

        return m;
    }

    /**
     * OVAL
     * Characteristics:
     *  - Face is slightly longer than wide (r1 ~0.65–0.75)
     *  - Forehead slightly wider than jaw (r3 > r2, but not by much)
     *  - Jaw is rounded, not too wide (r2 ~0.70–0.82)
     *  - Cheekbones are the widest point
     */
    private static float membershipOval(FaceRatios r) {
        float f1 = trapezoid(r.r1_widthToHeight, 0.52f, 0.60f, 0.80f, 0.88f);
        float f2 = trapezoid(r.r2_jawToCheek, 0.55f, 0.65f, 0.85f, 0.95f);
        float f3 = trapezoid(r.r3_foreheadToCheek, 0.55f, 0.65f, 0.88f, 0.98f);
        float f4 = trapezoid(r.jawCurve, 0.40f, 0.55f, 0.85f, 0.95f);

        // Change: Multiplied for strictness
        return (f1 * f2 * f3 * f4)/4.0f;
    }

    /**
     * ROUND
     * Characteristics:
     *  - Width close to height (r1 > 0.80)
     *  - Jaw and forehead similar width, both close to cheek width
     *  - Full, soft jawline (high jawCurve)
     *  - Low elongation (r5 < 1.2)
     */
    private static float membershipRound(FaceRatios r) {
        float f1 = trapezoid(r.r1_widthToHeight, 0.90f, 0.98f, 1.05f, 1.15f); // Height approx Cheek
        float f2 = (r.cheekboneWidth > r.jawWidth) ? 1.0f : 0.0f; // Cheeks > Jaw
        float f3 = trapezoid(r.jawCurve, 0.60f, 0.75f, 1.00f, 1.00f); // Soft jawline
        return (f1 + f2 + f3) / 3.0f;
    }

    /**
     * SQUARE
     * Characteristics:
     *  - Width close to height (r1 ~0.80–0.95)
     *  - Jaw width close to cheekbone width (r2 > 0.85)
     *  - Forehead width close to cheekbone width (r3 > 0.85)
     *  - Flat, angular jaw (low jawCurve)
     *  - All three measurements (forehead, cheek, jaw) are similar
     */
    private static float membershipSquare(FaceRatios r) {
        float f1 = trapezoid(r.r1_widthToHeight, 0.85f, 0.95f, 1.05f, 1.15f); // Width approx Height
        float f2 = trapezoid(r.r2_jawToCheek, 0.85f, 0.92f, 1.00f, 1.10f);    // Jaw approx Cheek
        float f3 = trapezoid(r.r3_foreheadToCheek, 0.85f, 0.92f, 1.00f, 1.10f); // Forehead approx Cheek
        return (f1 + f2 + f3) / 3.0f;
    }

    /**
     * HEART (Inverted Triangle)
     * Characteristics:
     *  - Forehead is significantly wider than jaw (r3 >> r2)
     *  - Narrow, pointed chin (low jawCurve)
     *  - Cheekbones are the widest point
     *  - r4 (jaw/forehead) is low
     */
    private static float membershipHeart(FaceRatios r) {
        float f1 = (r.foreheadWidth > r.cheekboneWidth) ? 1.0f : 0.0f;
        float f2 = (r.cheekboneWidth > r.jawWidth) ? 1.0f : 0.0f;
        float f3 = trapezoid(r.jawCurve, 0.10f, 0.20f, 0.45f, 0.55f);
        return (f1 + f2 + f3) / 3.0f;
    }

    /**
     * TRIANGLE (Pear) - STRICT
     * Logic: Jawline > Cheeks > Forehead.
     * Using multiplication makes it strict: all conditions must be true.
     */
    private static float membershipTriangle(FaceRatios r) {
        // 1. Boolean check: Jaw MUST be wider than Cheeks
        float f1 = (r.jawWidth > r.cheekboneWidth) ? 1.0f : 0.0f;

        // 2. Boolean check: Cheeks MUST be wider than Forehead
        float f2 = (r.cheekboneWidth > r.foreheadWidth) ? 1.0f : 0.0f;

        // 3. Ratio check: Jaw vs Forehead must be significantly wider (e.g., > 1.1x)
        // We use trapezoid to create a strict "gate"
        float f3 = trapezoid(r.r4_jawToForehead, 1.05f, 1.15f, 1.60f, 2.00f);

        // 4. Jaw Curve: Triangle usually has a flatter/wider jaw base than Heart or Oval
        float f4 = trapezoid(r.jawCurve, 0.50f, 0.65f, 1.00f, 1.00f);

        // CHANGE: Use multiplication (*) instead of addition (+) to make it strict.
        // If f1 or f2 is 0.0, the whole result is 0.0.
        return (f1 + f2 + f3 + f4)/4.0f;
    }

    /**
     * OBLONG (Rectangle)
     * Characteristics:
     *  - Face significantly longer than wide (r5 > 1.4)
     *  - All three widths relatively similar (forehead ≈ cheek ≈ jaw)
     *  - r1 is low (not wide relative to height)
     */
    private static float membershipOblong(FaceRatios r) {
        float f1 = trapezoid(r.r5_elongation, 1.25f, 1.40f, 2.00f, 2.50f); // Height is largest
        float f2 = trapezoid(r.r3_foreheadToCheek, 0.80f, 0.90f, 1.10f, 1.20f); // Forehead similar to cheek
        float f3 = (r.foreheadWidth > r.jawWidth) ? 1.0f : 0.0f; // Forehead > Jaw
        return (f1 + f2 + f3) / 3.0f;
    }

    /**
     * DIAMOND
     * Characteristics:
     *  - Cheekbones are widest — wider than both forehead and jaw
     *  - Narrow forehead AND narrow jaw (both < cheeks)
     *  - r3 and r2 both low
     *  - Pointed chin (low jawCurve)
     */
    private static float membershipDiamond(FaceRatios r) {
        float f1 = (r.faceHeight > r.cheekboneWidth) ? 1.0f : 0.0f;
        float f2 = (r.cheekboneWidth > r.foreheadWidth) ? 1.0f : 0.0f;
        float f3 = (r.foreheadWidth > r.jawWidth) ? 1.0f : 0.0f;
        float f4 = trapezoid(r.jawCurve, 0.10f, 0.25f, 0.50f, 0.65f); // Pointed
        return (f1 + f2 + f3 + f4) / 4.0f;
    }

    // ─── Step 3: Build result from fuzzy outputs ───────────────────────────────

    private static FaceShapeResult buildResult(Map<String, Float> memberships,
                                               FaceRatios ratios) {
        // Sort by membership descending
        List<ShapeMembership> sorted = new ArrayList<>();
        for (Map.Entry<String, Float> entry : memberships.entrySet()) {
            sorted.add(new ShapeMembership(entry.getKey(), entry.getValue()));
        }
        sorted.sort((a, b) -> Float.compare(b.membership, a.membership));

        ShapeMembership top = sorted.get(0);

        // Normalize confidence: scale top membership to 0–100
        // If top is very low, it means ambiguous detection
        float totalMembership = 0f;
        for (ShapeMembership sm : sorted) totalMembership += sm.membership;

        int confidence;
        if (totalMembership < 0.0001f) {
            confidence = 30; // No strong match — default low confidence
        } else {
            // Confidence = proportion of top membership vs total
            confidence = (int) Math.min(95, (top.membership / totalMembership) * 100 * 1.8f);
            confidence = Math.max(confidence, 35);
        }

        String explanation = buildExplanation(top.shape, sorted, ratios);

        return new FaceShapeResult(top.shape, confidence, sorted, ratios, explanation);
    }

    private static String buildExplanation(String primaryShape,
                                           List<ShapeMembership> sorted,
                                           FaceRatios r) {
        StringBuilder sb = new StringBuilder();

        sb.append("Detected: ").append(primaryShape).append("\n\n");

        // Ratio summary
        sb.append(String.format("Forehead : %.0f%%  Cheeks : %.0f%%  Jaw : %.0f%%\n",
                r.r3_foreheadToCheek * 100,
                100f,
                r.r2_jawToCheek * 100));

        sb.append(String.format("Width/Height ratio : %.2f\n", r.r1_widthToHeight));
        sb.append(String.format("Jaw curve score    : %.2f\n\n", r.jawCurve));

        // Shape breakdown
        sb.append("Shape likelihood:\n");
        for (ShapeMembership sm : sorted) {
            int pct = (int)(sm.membership * 100);
            sb.append(String.format("  %-9s %d%%\n", sm.shape, pct));
        }

        return sb.toString();
    }

    // ─── Fuzzy membership function helpers ────────────────────────────────────

    /**
     * Trapezoidal membership function.
     * Returns 1.0 in [b, c], linearly rises from a→b, linearly falls from c→d.
     */
    private static float trapezoid(float x, float a, float b, float c, float d) {
        if (x <= a || x >= d) return 0f;
        if (x >= b && x <= c) return 1f;
        if (x < b) return (x - a) / (b - a);
        return (d - x) / (d - c);
    }

    /**
     * Triangular membership function.
     * Peaks at b, zero outside [a, c].
     */
    private static float triangle(float x, float a, float b, float c) {
        return trapezoid(x, a, b, b, c);
    }

    // ─── Euclidean distance between two normalized landmarks ──────────────────

    private static float dist(NormalizedLandmark a, NormalizedLandmark b) {
        float dx = a.x() - b.x();
        float dy = a.y() - b.y();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }


}