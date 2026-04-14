package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class SavedStylesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations); // Reuses the grid layout

        RecyclerView recyclerView = findViewById(R.id.recyclerStyles);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Fetch only saved styles from your manager
        List<Hairstyle> savedList = SavedStylesManager.getSavedStyles(this);

        HairstyleAdapter adapter = new HairstyleAdapter(savedList, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Setup Navigation
        bottomNav.setSelectedItemId(R.id.nav_saved);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_scan) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, RecommendationsActivity.class));
                return true;
            }
            return true;
        });
    }
}