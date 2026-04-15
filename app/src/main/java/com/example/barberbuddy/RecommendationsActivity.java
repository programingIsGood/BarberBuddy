package com.example.barberbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    private List<Hairstyle> fullList = new ArrayList<>();
    private List<Hairstyle> displayList = new ArrayList<>();
    private HairstyleAdapter adapter;

    private String currentQuery = "";
    private String currentFilter = "All";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        prefs = getSharedPreferences("barberbuddy_prefs", MODE_PRIVATE);

        RecyclerView recyclerView = findViewById(R.id.recyclerStyles);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        TextInputEditText etSearch = findViewById(R.id.etSearch);
        ChipGroup chipGroup = findViewById(R.id.filterChipGroup);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new HairstyleAdapter(displayList, hairstyle -> {
            Intent intent = new Intent(this, StyleDetailActivity.class);
            intent.putExtra("HAIRSTYLE_ID", hairstyle.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        setupSearch(etSearch);
        setupFilters(chipGroup);
        setupBottomNav(bottomNav);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // ─────────────────────────────
    // LOAD DATA (CORE FIX)
    // ─────────────────────────────
    private void loadData() {

        fullList.clear();

        String faceShape = prefs.getString("last_face_shape", null);
        String secondary = prefs.getString("last_secondary_shape", null);

        if (faceShape != null && !faceShape.isEmpty()) {

            fullList.addAll(HairstyleRepository.getForFaceShape(faceShape));

            if (secondary != null && !secondary.isEmpty()) {
                List<Hairstyle> secondaryList =
                        HairstyleRepository.getForFaceShape(secondary);

                for (Hairstyle s : secondaryList) {
                    if (!listContainsId(fullList, s.getId())) {
                        fullList.add(s);
                    }
                }
            }

        } else {
            fullList.addAll(HairstyleRepository.getAll());
        }

        applyFilters();
    }

    // ─────────────────────────────
    // SEARCH
    // ─────────────────────────────
    private void setupSearch(TextInputEditText etSearch) {

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }
        });
    }

    // ─────────────────────────────
    // FILTERS
    // ─────────────────────────────
    private void setupFilters(ChipGroup chipGroup) {

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

            if (checkedIds.isEmpty()) {
                currentFilter = "All";
            } else {
                int id = checkedIds.get(0);

                if (id == R.id.chipAll) currentFilter = "All";
                else if (id == R.id.chipShort) currentFilter = "Short";
                else if (id == R.id.chipMedium) currentFilter = "Medium";
                else if (id == R.id.chipTrending) currentFilter = "Trending";
                else if (id == R.id.chipClassic) currentFilter = "Classic";
                else currentFilter = "All";
            }

            applyFilters();
        });
    }

    // ─────────────────────────────
    // APPLY FILTER LOGIC
    // ─────────────────────────────
    private void applyFilters() {

        displayList.clear();

        for (Hairstyle h : fullList) {

            boolean matchesSearch =
                    currentQuery.isEmpty()
                            || h.getName().toLowerCase().contains(currentQuery)
                            || (h.getTrend() != null && h.getTrend().toLowerCase().contains(currentQuery))
                            || (h.getMaintenanceLevel() != null && h.getMaintenanceLevel().toLowerCase().contains(currentQuery));

            boolean matchesFilter;

            switch (currentFilter) {
                case "Short":
                    matchesFilter = "Low".equalsIgnoreCase(h.getMaintenanceLevel());
                    break;

                case "Medium":
                    matchesFilter = "Medium".equalsIgnoreCase(h.getMaintenanceLevel());
                    break;

                case "Trending":
                    matchesFilter = "Trending".equalsIgnoreCase(h.getTrend());
                    break;

                case "Classic":
                    matchesFilter = "Classic".equalsIgnoreCase(h.getTrend());
                    break;

                default:
                    matchesFilter = true;
            }

            if (matchesSearch && matchesFilter) {
                displayList.add(h);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ─────────────────────────────
    // BOTTOM NAV
    // ─────────────────────────────
    private void setupBottomNav(BottomNavigationView bottomNav) {

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_scan) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedStylesActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    // ─────────────────────────────
    // HELPER
    // ─────────────────────────────
    private boolean listContainsId(List<Hairstyle> list, int id) {
        for (Hairstyle h : list) {
            if (h.getId() == id) return true;
        }
        return false;
    }
}