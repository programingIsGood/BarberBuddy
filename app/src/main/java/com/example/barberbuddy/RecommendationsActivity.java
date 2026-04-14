package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    private List<Hairstyle> fullList = new ArrayList<>();
    private HairstyleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // 1. GET INTENT DATA
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        // 2. INITIALIZE VIEWS
        RecyclerView recyclerView = findViewById(R.id.recyclerStyles);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // 3. FETCH BASE DATA
        if (faceShape != null) {
            fullList.addAll(HairstyleRepository.getForFaceShape(faceShape));
        }

        // 4. MERGE SECONDARY SHAPE (Avoiding Duplicates)
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryList = HairstyleRepository.getForFaceShape(secondary);
            for (Hairstyle s : secondaryList) {
                if (!listContainsId(fullList, s.getId())) {
                    fullList.add(s);
                }
            }
        }

        // 5. SET UP ADAPTER
        adapter = new HairstyleAdapter(fullList, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // 6. FIX BOTTOM NAVIGATION
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_scan) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }

            if (id == R.id.nav_saved) {
                // FIXED: Now opens the SavedStylesActivity instead of a Toast
                startActivity(new Intent(this, SavedStylesActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("FACE_SHAPE", faceShape);
                intent.putExtra("SECONDARY_SHAPE", secondary);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    /**
     * Helper to check if a hairstyle ID already exists in our list
     */
    private boolean listContainsId(List<Hairstyle> list, int id) {
        for (Hairstyle h : list) {
            if (h.getId() == id) return true;
        }
        return false;
    }
}