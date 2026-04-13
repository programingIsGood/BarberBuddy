package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // 1. Get Intent data
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        RecyclerView recyclerView = findViewById(R.id.recyclerAllRecommendations);

        // 2. Fetch recommendations using the FIXED repository method
        // We wrap in new ArrayList to prevent "multiply styles" bug
        List<Hairstyle> recommended = new ArrayList<>(HairstyleRepository.getForFaceShape(faceShape));

        // 3. Handle secondary shape if it exists
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryStyles = HairstyleRepository.getForFaceShape(secondary);
            for (Hairstyle s : secondaryStyles) {
                // Add only if not already in the list
                boolean alreadyPresent = false;
                for (Hairstyle existing : recommended) {
                    if (existing.getId() == s.getId()) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (!alreadyPresent) {
                    recommended.add(s);
                }
            }
        }

        // 4. Setup Adapter
        HairstyleAdapter adapter = new HairstyleAdapter(recommended, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // 5. Navigation Setup
        setupNavigation(faceShape, secondary);
    }

    private void setupNavigation(String faceShape, String secondary) {
        // Scan button -> Main/Scan screen
        findViewById(R.id.navScan).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Profile button -> Back to Results (passing shapes back)
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("FACE_SHAPE", faceShape);
            intent.putExtra("SECONDARY_SHAPE", secondary);
            startActivity(intent);
        });

        // Saved button -> Standby
        findViewById(R.id.navSaved).setOnClickListener(v ->
                Toast.makeText(this, "Saved styles coming soon!", Toast.LENGTH_SHORT).show()
        );
    }
}