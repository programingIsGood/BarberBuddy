package com.example.barberbuddy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HairstyleRepository {

    private static final List<Hairstyle> ALL_STYLES = new ArrayList<>(Arrays.asList(
            new Hairstyle(1, "Textured Crop", R.drawable.style_textured_crop,
                    Arrays.asList("Round", "Square", "Triangle", "Diamond"),
                    "Short on sides with textured, choppy top. Adds angles and definition.",
                    "Low", "Apply matte clay to damp hair.", "Trending"),

            new Hairstyle(2, "Classic Pompadour", R.drawable.style_pompadour,
                    Arrays.asList("Round", "Oblong", "Heart", "Diamond"),
                    "High volume swept back from the forehead. Elongates rounder faces.",
                    "High", "Use strong-hold pomade and blow-dry upward.", "Classic"),

            new Hairstyle(3, "Buzz Cut", R.drawable.style_buzz_cut,
                    Arrays.asList("Oval", "Square", "Oblong", "Diamond"),
                    "Clean, ultra-short all over. Works best with angular features.",
                    "Low", "Trim every 2–3 weeks.", "Timeless"),

            new Hairstyle(4, "Side Part", R.drawable.style_side_part,
                    Arrays.asList("Oval", "Square", "Heart", "Triangle"),
                    "Clean gentleman's cut with a defined side part. Adds structure.",
                    "Medium", "Use a fine-tooth comb and light pomade.", "Classic"),

            new Hairstyle(5, "Modern Quiff", R.drawable.style_quiff,
                    Arrays.asList("Oval", "Round", "Heart", "Triangle"),
                    "Swept-up front volume with tapered sides. Versatile and modern.",
                    "Medium", "Blow-dry forward then push back at the front.", "Trending"),

            new Hairstyle(6, "Undercut", R.drawable.style_undercut,
                    Arrays.asList("Oval", "Square", "Oblong", "Diamond"),
                    "Shaved sides with long top. Creates bold contrast.",
                    "Medium", "Clean up sides weekly.", "Trending"),

            new Hairstyle(7, "Caesar Cut", R.drawable.style_caesar,
                    Arrays.asList("Oval", "Round", "Square", "Triangle"),
                    "Short, horizontally-fringed cut. Low-maintenance and flattering.",
                    "Low", "Light styling cream on damp hair.", "Classic"),

            new Hairstyle(8, "Slick Back", R.drawable.style_slick_back,
                    Arrays.asList("Oval", "Oblong", "Heart", "Diamond"),
                    "All hair combed straight back. Sophisticated and confident.",
                    "Medium", "Apply pomade and comb straight back.", "Classic")
    ));

    // FIXED: Smarter filtering that handles "Round Face" or "Round & Oval"
    public static List<Hairstyle> getForFaceShape(String inputShape) {
        List<Hairstyle> result = new ArrayList<>();
        if (inputShape == null || inputShape.isEmpty()) return result;

        String searchStr = inputShape.toLowerCase();

        for (Hairstyle h : ALL_STYLES) {
            for (String suitableShape : h.getSuitableFaceShapes()) {
                // Check if the input string contains the keyword (e.g., "Round" in "Round Face")
                if (searchStr.contains(suitableShape.toLowerCase())) {
                    result.add(h);
                    break; // Found a match for this hairstyle, move to next
                }
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
        return new ArrayList<>(ALL_STYLES);
    }
}