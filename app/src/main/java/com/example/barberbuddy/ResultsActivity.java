package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

        // Fallbacks for safety
        if (faceShape == null) faceShape = "Oval";

        // 2. Initialize UI Components
        TextView tvFaceShape = findViewById(R.id.tvFaceShapeResult);
        TextView tvFaceDesc  = findViewById(R.id.tvFaceShapeDesc);

        tvFaceShape.setText(secondary != null && !secondary.isEmpty() ?
                faceShape + " & " + secondary : faceShape + " Face");

        // 4. Set Description (Handle Secondary Shape logic)
        String baseDescription = DESCRIPTIONS.get(faceShape);
        tvFaceDesc.setText(secondary != null && !secondary.isEmpty() ?
                "Your face blends " + faceShape + " and " + secondary + " features.\n\n" + baseDescription :
                baseDescription);

        Button btnBrowse = findViewById(R.id.btnBrowse);
        String finalFaceShape = faceShape;
        btnBrowse.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendationsActivity.class);
            intent.putExtra("FACE_SHAPE", finalFaceShape);
            intent.putExtra("SECONDARY_SHAPE", secondary);
            startActivity(intent);
        });

        findViewById(R.id.btnRetry).setOnClickListener(v -> finish());
    }

}