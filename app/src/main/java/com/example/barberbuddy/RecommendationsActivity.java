package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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

        // INTENT DATA
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        String secondary = getIntent().getStringExtra("SECONDARY_SHAPE");

        // VIEW
        RecyclerView recyclerView = findViewById(R.id.recyclerStyles);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // BASE DATA
        fullList = new ArrayList<>(HairstyleRepository.getForFaceShape(faceShape));

        // MERGE SECONDARY SHAPE (NO DUPLICATES)
        if (secondary != null && !secondary.isEmpty()) {
            List<Hairstyle> secondaryList =
                    HairstyleRepository.getForFaceShape(secondary);

            for (Hairstyle s : secondaryList) {
                boolean exists = false;

                for (Hairstyle h : fullList) {
                    if (h.getId() == s.getId()) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    fullList.add(s);
                }
            }
        }

        // ADAPTER
        adapter = new HairstyleAdapter(fullList, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // BOTTOM NAVIGATION (MATCH XML IDs)
        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_scan) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }

            if (id == R.id.nav_saved) {
                Toast.makeText(this, "Saved styles coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ResultsActivity.class);
                intent.putExtra("FACE_SHAPE", faceShape);
                intent.putExtra("SECONDARY_SHAPE", secondary);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}