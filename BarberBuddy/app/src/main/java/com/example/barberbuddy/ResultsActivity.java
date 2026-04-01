package com.example.barberbuddy;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();
    static {
        DESCRIPTIONS.put("Oval",
                "Oval is the most versatile face shape — almost every hairstyle works for you.");
        DESCRIPTIONS.put("Round",
                "Add height on top and keep sides close to elongate and define your face.");
        DESCRIPTIONS.put("Square",
                "Textured or layered tops soften strong jawlines and add movement.");
        DESCRIPTIONS.put("Heart",
                "Fuller sides with shorter tops balance a wider forehead perfectly.");
        DESCRIPTIONS.put("Oblong",
                "Add width with textured sides. Avoid extra height which elongates further.");
        DESCRIPTIONS.put("Diamond",
                "You have striking cheekbones. Styles with forehead volume and chin-length sides balance your look.");
        DESCRIPTIONS.put("Triangle",
                "Your jaw is your most prominent feature. Short hairstyles with volume on top balance your look.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // 1. Retrieve all Intent extras at once
        String faceShape    = getIntent().getStringExtra("FACE_SHAPE");
        String secondary    = getIntent().getStringExtra("SECONDARY_SHAPE"); // may be null
        int confidence      = getIntent().getIntExtra("CONFIDENCE", 0);
        HashMap<String, Integer> fuzzyScores =
                (HashMap<String, Integer>) getIntent().getSerializableExtra("FUZZY_SCORES");

        // Fallbacks for safety
        if (faceShape == null) faceShape = "Oval";
        if (fuzzyScores == null) fuzzyScores = new HashMap<>();

        // 2. Initialize UI Components
        TextView tvFaceShape = findViewById(R.id.tvFaceShapeResult);
        TextView tvFaceDesc  = findViewById(R.id.tvFaceShapeDesc);
        RecyclerView recycler = findViewById(R.id.recyclerStyles);
        LinearLayout scoresContainer = findViewById(R.id.scoresContainer);

        // 3. Set Header Text (Face Shape + Confidence)
        // If secondary exists, we show a "Dual" result title
        if (secondary != null && !secondary.isEmpty()) {
            tvFaceShape.setText(faceShape + " & " + secondary);
        } else {
            tvFaceShape.setText(faceShape + " Face");
        }

        // 4. Set Description (Handle Secondary Shape logic)
        String baseDescription = DESCRIPTIONS.get(faceShape);
        if (secondary != null && !secondary.isEmpty()) {
            tvFaceDesc.setText("Your face blends " + faceShape + " and "
                    + secondary + " features.\n\n" + baseDescription);
        } else {
            tvFaceDesc.setText(baseDescription);
        }

        // 5. Build the Dynamic Score Bars
        buildScoreBars(scoresContainer, fuzzyScores);

        // 6. Populate Hairstyles: Merge recommendations from both shapes
        List<Hairstyle> recommended = HairstyleRepository.getForFaceShape(faceShape);
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryStyles = HairstyleRepository.getForFaceShape(secondary);
            for (Hairstyle s : secondaryStyles) {
                // Prevent duplicate styles from appearing in the grid
                if (!recommended.contains(s)) {
                    recommended.add(s);
                }
            }
        }

        // 7. Setup RecyclerView
        HairstyleAdapter adapter = new HairstyleAdapter(recommended, hairstyle -> {
            android.content.Intent intent = new android.content.Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        recycler.setAdapter(adapter);
    }

    private void buildScoreBars(LinearLayout container,
                                Map<String, Integer> scores) {
        String[] order = {"Oval","Round","Square","Heart","Oblong","Diamond","Triangle"};
        container.removeAllViews();

        for (String shape : order) {
            int score = scores.containsKey(shape) ? scores.get(shape) : 0;
            if (score == 0) continue;

            // Row: label + bar + percentage
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 6, 0, 6);

            TextView label = new TextView(this);
            label.setText(shape);
            label.setTextColor(0xFFFFFFFF);
            label.setTextSize(13f);
            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT);
            label.setLayoutParams(labelParams);
            row.addView(label);

            ProgressBar bar = new ProgressBar(this, null,
                    android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress(score);
            bar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFF00E5FF));
            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(0, 36, 1f);
            barParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            bar.setLayoutParams(barParams);
            row.addView(bar);

            TextView pct = new TextView(this);
            pct.setText(score + "%");
            pct.setTextColor(0xFF00E5FF);
            pct.setTextSize(13f);
            pct.setPadding(12, 0, 0, 0);
            LinearLayout.LayoutParams pctParams =
                    new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
            pctParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            pct.setLayoutParams(pctParams);
            row.addView(pct);

            container.addView(row);
        }
    }
}