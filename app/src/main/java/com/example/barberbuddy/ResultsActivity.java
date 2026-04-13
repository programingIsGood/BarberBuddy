package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
        DESCRIPTIONS.put("Diamond", "You have striking cheekbones. Styles with forehead volume and chin-length sides balance your look.");
        DESCRIPTIONS.put("Triangle", "Your jaw is your most prominent feature. Short hairstyles with volume on top balance your look.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // 1. Retrieve Extras
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        if (faceShape == null) faceShape = "Oval";

        // 2. Setup UI
        TextView tvFaceShape = findViewById(R.id.tvFaceShapeResult);
        TextView tvFaceDesc  = findViewById(R.id.tvFaceShapeDesc);

        tvFaceShape.setText(secondary != null && !secondary.isEmpty() ?
                faceShape + " & " + secondary : faceShape);

        String baseDescription = DESCRIPTIONS.get(faceShape);
        tvFaceDesc.setText(secondary != null && !secondary.isEmpty() ?
                "Your face blends " + faceShape + " and " + secondary + " features.\n\n" + baseDescription :
                baseDescription);

        // 3. Browse Button: Go to Recommendations
        Button btnBrowse = findViewById(R.id.btnBrowse);
        String finalFaceShape = faceShape;
        btnBrowse.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendationsActivity.class);
            intent.putExtra("FACE_SHAPE", finalFaceShape);
            intent.putExtra("SECONDARY_SHAPE", secondary);
            startActivity(intent);
        });

        // 4. Retry Button: Always go to Scan screen
        Button btnRetry = findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(v -> {
            // Replace 'ScanActivity' with the actual name of your scanning class
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);

            // FLAG_ACTIVITY_CLEAR_TOP: If ScanActivity is already in the stack,
            // bring it to the top and close this Results screen.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish(); // Closes the results page so they can't "back" into it
        });
    }
}