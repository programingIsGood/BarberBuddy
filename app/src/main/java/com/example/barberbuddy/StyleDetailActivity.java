package com.example.barberbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class StyleDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_detail);

        // 1. Get the ID passed from the clicked card
        int id = getIntent().getIntExtra("HAIRSTYLE_ID", -1);

        // 2. Fetch the specific hairstyle data using that ID
        Hairstyle h = HairstyleRepository.getById(id);

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

        Glide.with(this).load(h.getImageRes()).centerCrop().into(imgStyle);

        tvName.setText(h.getName());
        tvDescription.setText(h.getDescription());
        tvMaintenance.setText("Maintenance Level: " + h.getMaintenanceLevel());
        tvTips.setText(h.getMaintenanceTips());
        tvTrend.setText(h.getTrend());

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
    }
}