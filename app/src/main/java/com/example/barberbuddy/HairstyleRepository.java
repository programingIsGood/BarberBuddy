package com.example.barberbuddy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HairstyleRepository {

    // DEFINED ONLY ONCE
    // Inside HairstyleRepository.java
    private static final List<Hairstyle> ALL_STYLES = new ArrayList<>(Arrays.asList(
            new Hairstyle(1, "Textured Crop",
                    R.drawable.style_textured_crop,
                    R.drawable.hair_pomp_overlay,
                    Arrays.asList("Round", "Square", "Triangle"), // Fixed caret here
                    "Short on sides with textured, choppy top.",
                    "Low", "Apply matte clay.", "Trending",
                    "models/textured_crop.glb"), // Added model path

            new Hairstyle(2, "Classic Pompadour",
                    R.drawable.style_pompadour,
                    R.drawable.hair_pomp_overlay,
                    Arrays.asList("Round", "Oblong", "Heart"),
                    "High volume swept back.",
                    "High", "Use strong-hold pomade.", "Classic",
                    "models/pompadour.glb")
            // Repeat for other styles...
    ));

    public static List<Hairstyle> getRecommendations(FaceShapeAnalyzer.FaceShapeResult result) {
        Map<Integer, Float> scoreMap = new HashMap<>();
        for (FaceShapeAnalyzer.ShapeMembership sm : result.allMemberships) {
            if (sm.membership < 0.05f) continue;
            for (Hairstyle h : ALL_STYLES) {
                if (h.getSuitableFaceShapes().contains(sm.shape)) {
                    float current = scoreMap.getOrDefault(h.getId(), 0f);
                    scoreMap.put(h.getId(), current + sm.membership);
                }
            }
        }
        List<Hairstyle> resultList = new ArrayList<>();
        for (Hairstyle h : ALL_STYLES) {
            if (scoreMap.containsKey(h.getId())) {
                resultList.add(h);
            }
        }
        resultList.sort((a, b) ->
                Float.compare(scoreMap.getOrDefault(b.getId(), 0f),
                        scoreMap.getOrDefault(a.getId(), 0f)));
        return resultList;
    }

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