package com.example.barberbuddy;

import java.util.*;

public class HairstyleRepository {

    private static final List<Hairstyle> ALL_STYLES = new ArrayList<>(Arrays.asList(

            new Hairstyle(1, "Textured Crop",
                    R.drawable.style_textured_crop,
                    R.drawable.hair_crop_overlay,
                    Arrays.asList("Round", "Square", "Triangle", "Diamond"),
                    "Short on sides with textured top.",
                    "Low", "Use matte clay.", "Trending",
                    "models/textured_crop.glb",
                    false),

            new Hairstyle(2, "Classic Pompadour",
                    R.drawable.style_pompadour,
                    R.drawable.hair_pomp_overlay,
                    Arrays.asList("Round", "Oblong", "Heart", "Diamond"),
                    "High volume swept back.",
                    "High", "Use pomade and blow-dry upward.",
                    "Classic",
                    "models/pompadour.glb",
                    false),

            new Hairstyle(3, "Buzz Cut",
                    R.drawable.style_buzz_cut,
                    R.drawable.hair_buzz_overlay,
                    Arrays.asList("Oval", "Square", "Oblong", "Diamond"),
                    "Clean ultra-short cut.",
                    "Low", "Trim every 2–3 weeks.",
                    "Timeless",
                    "models/buzz_cut.glb",
                    false),

            new Hairstyle(4, "Side Part",
                    R.drawable.style_side_part,
                    R.drawable.hair_sidepart_overlay,
                    Arrays.asList("Oval", "Square", "Heart", "Triangle"),
                    "Classic gentleman style.",
                    "Medium", "Use light pomade.",
                    "Classic",
                    "models/side_part.glb",
                    false),

            new Hairstyle(5, "Modern Quiff",
                    R.drawable.style_quiff,
                    R.drawable.hair_quiff_overlay,
                    Arrays.asList("Oval", "Round", "Heart", "Triangle"),
                    "Volume at the front.",
                    "Medium", "Blow-dry forward then lift.",
                    "Trending",
                    "models/quiff.glb",
                    false),

            new Hairstyle(6, "Undercut",
                    R.drawable.style_undercut,
                    R.drawable.hair_undercut_overlay,
                    Arrays.asList("Oval", "Square", "Oblong", "Diamond"),
                    "Shaved sides with long top.",
                    "Medium", "Clean sides weekly.",
                    "Trending",
                    "models/undercut.glb",
                    false),

            new Hairstyle(7, "Caesar Cut",
                    R.drawable.style_caesar,
                    R.drawable.hair_caesar_overlay,
                    Arrays.asList("Oval", "Round", "Square", "Triangle"),
                    "Short fringe style.",
                    "Low", "Light styling cream.",
                    "Classic",
                    "models/caesar.glb",
                    false),

            new Hairstyle(8, "Slick Back",
                    R.drawable.style_slick_back,
                    R.drawable.hair_slick_overlay,
                    Arrays.asList("Oval", "Oblong", "Heart", "Diamond"),
                    "Hair combed straight back.",
                    "Medium", "Apply pomade.",
                    "Classic",
                    "models/slick_back.glb",
                    false),

            // ───────────── ASIAN STYLES ─────────────

            new Hairstyle(9, "Korean Two-Block",
                    R.drawable.style_two_block,
                    R.drawable.hair_undercut_overlay,
                    Arrays.asList("Oval", "Heart", "Oblong", "Diamond"),
                    "Volume top with short sides.",
                    "Medium", "Texturizing spray + blow-dry.",
                    "Trending",
                    "models/twoblock.glb",
                    true),

            new Hairstyle(10, "Curtain Fringe",
                    R.drawable.style_curtain_fringe,
                    R.drawable.hair_sidepart_overlay,
                    Arrays.asList("Oval", "Heart", "Round", "Triangle"),
                    "Soft parted fringe.",
                    "Low", "Use light serum.",
                    "Trending",
                    "models/curtain.glb",
                    true),

            new Hairstyle(11, "Korean Perm",
                    R.drawable.style_korean_perm,
                    R.drawable.hair_quiff_overlay,
                    Arrays.asList("Oval", "Square", "Oblong", "Diamond"),
                    "Soft wavy volume.",
                    "Medium", "Use curl cream.",
                    "Trending",
                    "models/korean_perm.glb",
                    true),

            new Hairstyle(12, "Japanese Wolf Cut",
                    R.drawable.style_japanese_wolfcut,
                    R.drawable.hair_crop_overlay,
                    Arrays.asList("Oval", "Round", "Heart", "Square"),
                    "Layered shaggy style.",
                    "Medium", "Use texturizing spray.",
                    "Trending",
                    "models/wolfcut.glb",
                    true),

            new Hairstyle(13, "Asian High Fade",
                    R.drawable.style_high_fade,
                    R.drawable.hair_buzz_overlay,
                    Arrays.asList("Oval", "Square", "Round", "Triangle"),
                    "High fade clean look.",
                    "Low", "Touch up fade.",
                    "Classic",
                    "models/highfade.glb",
                    true),

            new Hairstyle(14, "Asian Textured Fringe",
                    R.drawable.style_textured_fringe,
                    R.drawable.hair_caesar_overlay,
                    Arrays.asList("Heart", "Oval", "Oblong", "Diamond"),
                    "Choppy fringe style.",
                    "Low", "Matte paste.",
                    "Trending",
                    "models/fringe.glb",
                    true),

            new Hairstyle(15, "K-Drama Pushed Back",
                    R.drawable.style_pushed_back,
                    R.drawable.hair_slick_overlay,
                    Arrays.asList("Oval", "Square", "Diamond", "Oblong"),
                    "Soft pushed back look.",
                    "Medium", "Light pomade.",
                    "Classic",
                    "models/kdrama.glb",
                    true),

            new Hairstyle(16, "Asian Taper Fade",
                    R.drawable.style_taper_fade,
                    R.drawable.hair_crop_overlay,
                    Arrays.asList("Round", "Square", "Oval", "Triangle"),
                    "Clean taper fade.",
                    "Low", "Matte clay.",
                    "Classic",
                    "models/taper.glb",
                    true),

            new Hairstyle(17, "Modern Bowl Cut",
                    R.drawable.style_bowl_cut,
                    R.drawable.hair_caesar_overlay,
                    Arrays.asList("Oval", "Round", "Heart", "Diamond"),
                    "Soft bowl shape.",
                    "Low", "Trim regularly.",
                    "Trending",
                    "models/bowl.glb",
                    true),

            new Hairstyle(18, "Mullet Fade",
                    R.drawable.style_mullet_fade,
                    R.drawable.hair_undercut_overlay,
                    Arrays.asList("Oval", "Square", "Heart", "Round"),
                    "Modern mullet style.",
                    "Medium", "Use salt spray.",
                    "Trending",
                    "models/mullet.glb",
                    true)
    ));

    // ─────────────────────────────
    // RECOMMENDATION ENGINE
    // ─────────────────────────────
    public static List<Hairstyle> getRecommendations(
            FaceShapeAnalyzer.FaceShapeResult result,
            boolean asianContext) {

        if (result == null) return getAll();

        Map<Integer, Float> scoreMap = new HashMap<>();

        for (FaceShapeAnalyzer.ShapeMembership sm : result.allMemberships) {

            if (sm.membership < 0.05f) continue;

            for (Hairstyle h : ALL_STYLES) {

                if (h.getSuitableFaceShapes().contains(sm.shape)) {

                    float score = scoreMap.getOrDefault(h.getId(), 0f);
                    score += sm.membership;

                    if (asianContext && h.isAsianStyle()) {
                        score += 0.3f;
                    }

                    scoreMap.put(h.getId(), score);
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
                Float.compare(
                        scoreMap.getOrDefault(b.getId(), 0f),
                        scoreMap.getOrDefault(a.getId(), 0f)
                )
        );

        return resultList;
    }

    public static List<Hairstyle> getAll() {
        return new ArrayList<>(ALL_STYLES);
    }

    public static Hairstyle getById(int id) {
        for (Hairstyle h : ALL_STYLES) {
            if (h.getId() == id) return h;
        }
        return null;
    }
}