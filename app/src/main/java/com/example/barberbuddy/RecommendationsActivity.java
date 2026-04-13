package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // 1. Get the detected shapes from the previous screen
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        RecyclerView recyclerView = findViewById(R.id.recyclerAllRecommendations);

        // 2. Fetch recommendations from the Repository
        List<Hairstyle> recommended = HairstyleRepository.getForFaceShape(faceShape);

        // If there's a secondary shape, merge those styles too
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryStyles = HairstyleRepository.getForFaceShape(secondary);
            for (Hairstyle s : secondaryStyles) {
                if (!recommended.contains(s)) {
                    recommended.add(s);
                }
            }
        }

        // 3. Setup the Click Listener: This is the "Clickable Card" logic
        HairstyleAdapter adapter = new HairstyleAdapter(recommended, hairstyle -> {
            // When a card is clicked, we pass the specific Hairstyle ID to the detail activity
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}