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

    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_styles);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerSaved);
        emptyView = findViewById(R.id.llEmpty);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadSavedStyles();

        // Bottom Navigation
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

    private void loadSavedStyles() {
        List<Hairstyle> savedList = SavedStylesManager.getSavedStyles(this);

        if (savedList == null || savedList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        HairstyleAdapter adapter = new HairstyleAdapter(savedList, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload in case user added/removed saved styles
        loadSavedStyles();
    }
}