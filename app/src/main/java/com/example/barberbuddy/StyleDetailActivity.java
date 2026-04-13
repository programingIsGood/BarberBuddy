package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class StyleDetailActivity extends AppCompatActivity {

    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_detail);

        int hairstyleId = getIntent().getIntExtra("HAIRSTYLE_ID", -1);
        String faceShape = getIntent().getStringExtra("FACE_SHAPE");
        int confidence = getIntent().getIntExtra("CONFIDENCE", 0);

        Hairstyle h = HairstyleRepository.getById(hairstyleId);
        if (h == null) {
            finish();
            return;
        }

        // Views
        ImageView imgStyle = findViewById(R.id.imgStyleDetail);

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvDesc = findViewById(R.id.tvDetailDescription);
        TextView tvTips = findViewById(R.id.tvDetailTips);
        TextView tvTrend = findViewById(R.id.tvDetailTrend);
        TextView tvMaintChip = findViewById(R.id.tvDetailMaintenanceChip);
        TextView tvMaintLevel = findViewById(R.id.tvDetailMaintenanceLevelChip);
        TextView tvSuitable = findViewById(R.id.tvSuitableFor);
        TextView tvMatchBadge = findViewById(R.id.tvMatchBadge);
        // Safety check: if no hairstyle found, go back
        if (h == null) { finish(); return; }

        // 3. Update the UI
        ImageView imgStyle       = findViewById(R.id.imgStyleDetail);
        TextView tvName          = findViewById(R.id.tvDetailName);
        TextView tvDescription   = findViewById(R.id.tvDetailDescription);
        TextView tvMaintenance   = findViewById(R.id.tvDetailMaintenance);
        TextView tvTips          = findViewById(R.id.tvDetailTips);
        TextView tvTrend         = findViewById(R.id.tvDetailTrend);
        TextView tvSuitableFor   = findViewById(R.id.tvSuitableFor);

        ImageButton btnSave = findViewById(R.id.btnSaveDetail);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Load image
        Glide.with(this)
                .load(h.getImageRes())
                .centerCrop()
                .into(imgStyle);

        // Set text
        tvName.setText(h.getName());
        tvDesc.setText(h.getDescription());
        tvTips.setText(h.getMaintenanceTips());
        tvTrend.setText(h.getTrend() != null ? h.getTrend() : "Style");

        String level = h.getMaintenanceLevel();
        if (level == null) level = "Medium";

        tvMaintChip.setText(level.equals("Low") ? "Short" : "Medium+");
        tvMaintLevel.setText(level + " Maintenance");

        if (h.getSuitableFaceShapes() != null && !h.getSuitableFaceShapes().isEmpty()) {
            tvSuitable.setText(String.join(", ", h.getSuitableFaceShapes()));
        } else {
            tvSuitable.setText("All face shapes");
        }

        if (faceShape != null && confidence > 0) {
            tvMatchBadge.setText(confidence + "% match for " + faceShape + " Face");
        } else {
            tvMatchBadge.setText("Recommended style");
        }
        String shapes = String.join(", ", h.getSuitableFaceShapes());
        tvSuitableFor.setText("Best for: " + shapes);

        // Back button logic
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // --- FIXED: MOVE THIS SECTION INSIDE onCreate ---
        findViewById(R.id.btnTryOn).setOnClickListener(v -> {
            Intent intent = new Intent(this, TryOnActivity.class);
            intent.putExtra("HAIRSTYLE_ID", id);
            startActivity(intent);
        });
        // ------------------------------------------------
        // Back
        btnBack.setOnClickListener(v -> finish());

        // Save toggle
        isSaved = SavedStylesManager.isSaved(this, h.getId());
        updateSaveIcon(btnSave);

        btnSave.setOnClickListener(v -> {
            isSaved = !isSaved;

            if (isSaved) {
                SavedStylesManager.save(this, h.getId());
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                SavedStylesManager.remove(this, h.getId());
                Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
            }

            updateSaveIcon(btnSave);
        });

        // Share buttons
        findViewById(R.id.btnShareDetail).setOnClickListener(v -> shareStyle(h, faceShape));
        findViewById(R.id.btnShareStyle).setOnClickListener(v -> shareStyle(h, faceShape));

        // TRY IN AR (UPDATED)
        findViewById(R.id.btnTryAR).setOnClickListener(v -> {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSaveIcon(ImageButton btn) {
        btn.setImageResource(isSaved
                ? R.drawable.ic_bookmark_filled
                : R.drawable.ic_bookmark_outline);
    }

    private void shareStyle(Hairstyle h, String faceShape) {
        String text =
                "Check out the " + h.getName() + " hairstyle — perfect for " +
                        (faceShape != null ? faceShape + " faces" : "everyone") +
                        " ✂ BarberBuddy";

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(share, "Share this style"));
    }
}