package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        DESCRIPTIONS.put("Oval", "Oval is the most versatile face shape — almost every hairstyle works for you.");
        DESCRIPTIONS.put("Round", "Add height on top and keep sides close to elongate and define your face.");
        DESCRIPTIONS.put("Square", "Textured or layered tops soften strong jawlines and add movement.");
        DESCRIPTIONS.put("Heart", "Fuller sides with shorter tops balance a wider forehead perfectly.");
        DESCRIPTIONS.put("Oblong", "Add width with textured sides. Avoid extra height which elongates further.");
        DESCRIPTIONS.put("Diamond", "Cheekbones are dominant. Balance with volume on top.");
        DESCRIPTIONS.put("Triangle", "Jaw is dominant. Add volume on top for balance.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // INPUTS
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        if (faceShape == null || faceShape.isEmpty()) {
            faceShape = "Oval";
        }

        // FINAL COPIES FOR LAMBDAS (FIX ERROR)
        final String finalFaceShape = faceShape;
        final String finalSecondary = secondary;

        // VIEWS
        TextView tvFaceShapeResult = findViewById(R.id.tvFaceShapeResult);
        TextView tvFaceDesc = findViewById(R.id.tvFaceShapeDesc);

        TextView tvConfidence = findViewById(R.id.tvConfidenceValue);
        TextView tvStylesFound = findViewById(R.id.tvStylesFoundValue);
        TextView tvTrending = findViewById(R.id.tvTrendingValue);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnShare = findViewById(R.id.btnShare);

        TextView tvRetry = findViewById(R.id.tvRetry);

        // RESULT TEXT
        if (finalSecondary != null && !finalSecondary.isEmpty()) {
            tvFaceShapeResult.setText(finalFaceShape + "\n& " + finalSecondary);
        } else {
            tvFaceShapeResult.setText(finalFaceShape);
        }

        String description = DESCRIPTIONS.get(finalFaceShape);
        tvFaceDesc.setText(description != null ? description : "No description available.");

        // STATIC STATS (replace later with ML model)
        tvConfidence.setText("94%");
        tvStylesFound.setText("8");
        tvTrending.setText("3");

        // BACK BUTTON
        btnBack.setOnClickListener(v -> finish());

        // SHARE BUTTON (FIXED LAMBDA ERROR)
        btnShare.setOnClickListener(v -> {
            String text = "My face shape is " + finalFaceShape + " using BarberBuddy";

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);

            startActivity(Intent.createChooser(share, "Share result"));
        });

        // BROWSE HAIRSTYLES
        findViewById(R.id.btnBrowseHairstyles).setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendationsActivity.class);
            intent.putExtra("FACE_SHAPE", finalFaceShape);
            intent.putExtra("SECONDARY_SHAPE", finalSecondary);
            startActivity(intent);
        });

        // RETRY
        tvRetry.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}