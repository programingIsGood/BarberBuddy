package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        RecyclerView recyclerView = findViewById(R.id.recyclerAllRecommendations);

        // 1. Get recommendations for primary shape
        List<Hairstyle> recommended = HairstyleRepository.getForFaceShape(faceShape);

        // 2. If there's a secondary shape, merge those styles too
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryStyles = HairstyleRepository.getForFaceShape(secondary);
            for (Hairstyle s : secondaryStyles) {
                if (!recommended.contains(s)) {
                    recommended.add(s);
                }
            }
        }

        // 3. Setup the Adapter
        HairstyleAdapter adapter = new HairstyleAdapter(recommended, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}