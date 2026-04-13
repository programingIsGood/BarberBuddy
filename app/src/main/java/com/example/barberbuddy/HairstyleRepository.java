package com.example.barberbuddy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HairstyleRepository {

    public static List<Hairstyle> getRecommendations(FaceShapeAnalyzer.FaceShapeResult result) {
        Map<Integer, Float> scoreMap = new HashMap<>();

        // Weight each hairstyle by the sum of memberships of its matching shapes
        for (FaceShapeAnalyzer.ShapeMembership sm : result.allMemberships) {
            if (sm.membership < 0.05f) continue; // ignore negligible memberships

            for (Hairstyle h : ALL_STYLES) {
                if (h.getSuitableFaceShapes().contains(sm.shape)) {
                    float current = scoreMap.containsKey(h.getId())
                            ? scoreMap.get(h.getId()) : 0f;
                    scoreMap.put(h.getId(), current + sm.membership);
                }
            }
        }

        // Sort hairstyles by their total weighted score
        List<Hairstyle> result2 = new ArrayList<>();
        for (Hairstyle h : ALL_STYLES) {
            if (scoreMap.containsKey(h.getId())) {
                result2.add(h);
            }
        }
        result2.sort((a, b) ->
                Float.compare(scoreMap.getOrDefault(b.getId(), 0f),
                        scoreMap.getOrDefault(a.getId(), 0f)));

        return result2;
    }

    private static final List<Hairstyle> ALL_STYLES = new ArrayList<>(Arrays.asList(

            new Hairstyle(1, "Textured Crop", R.drawable.style_textured_crop,
                    Arrays.asList("Round", "Square","Triangle"),
                    "Short on sides with textured, choppy top. Adds angles and definition to softer, rounder faces.",
                    "Low",
                    "Apply matte clay to damp hair and rough-dry for texture. Trim sides every 3 weeks.",
                    "Trending"),

            new Hairstyle(2, "Classic Pompadour", R.drawable.style_pompadour,
                    Arrays.asList("Round", "Oblong", "Heart"),
                    "High volume swept back from the forehead. Elongates rounder faces and adds dramatic style.",
                    "High",
                    "Use strong-hold pomade. Blow-dry upward at the roots while brushing back.",
                    "Classic"),

            new Hairstyle(3, "Buzz Cut", R.drawable.style_buzz_cut,
                    Arrays.asList("Oval", "Square", "Oblong"),
                    "Clean, ultra-short all over. Works best with angular, well-defined facial features.",
                    "Low",
                    "Trim every 2–3 weeks to maintain clean lines. Use SPF on scalp.",
                    "Timeless"),

            new Hairstyle(4, "Side Part", R.drawable.style_side_part,
                    Arrays.asList("Oval", "Square", "Heart"),
                    "Clean gentleman's cut with a defined side part. Adds structure and a polished look.",
                    "Medium",
                    "Use a fine-tooth comb and light pomade daily. Re-part when damp.",
                    "Classic"),

            new Hairstyle(5, "Modern Quiff", R.drawable.style_quiff,
                    Arrays.asList("Oval", "Round", "Heart"),
                    "Swept-up front volume with tapered sides. Versatile, modern, and suitable for most settings.",
                    "Medium",
                    "Blow-dry forward then push back at the front. Use medium-hold wax.",
                    "Trending"),

            new Hairstyle(6, "Undercut", R.drawable.style_undercut,
                    Arrays.asList("Oval", "Square", "Oblong"),
                    "Shaved or very short sides with long top. Creates bold contrast and a sharp silhouette.",
                    "Medium",
                    "Clean up sides weekly. Style long top with pomade or wax as desired.",
                    "Trending"),

            new Hairstyle(7, "Caesar Cut", R.drawable.style_caesar,
                    Arrays.asList("Oval", "Round", "Square"),
                    "Short, horizontally-fringed cut with uniform length. Low-maintenance and universally flattering.",
                    "Low",
                    "Trim every 3–4 weeks. Light styling cream on damp hair for definition.",
                    "Classic"),

            new Hairstyle(8, "Slick Back", R.drawable.style_slick_back,
                    Arrays.asList("Oval", "Oblong", "Heart"),
                    "All hair combed straight back. Sophisticated, confident, and timeless.",
                    "Medium",
                    "Apply water-based pomade to damp hair. Comb straight back and let air-dry.",
                    "Classic")
    ));

    public static List<Hairstyle> getForFaceShape(String faceShape) {
        List<Hairstyle> result = new ArrayList<>();
        for (Hairstyle h : ALL_STYLES) {
            if (h.getSuitableFaceShapes().contains(faceShape)) {
                result.add(h);
            }
        }
        return result;
    }

    public static Hairstyle getById(int id) {
        for (Hairstyle h : ALL_STYLES) {
            if (h.getId() == id) return h;
        }
        return null;
    }

    public static List<Hairstyle> getAll() {
        return ALL_STYLES;
    }
}