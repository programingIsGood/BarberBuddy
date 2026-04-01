package com.example.barberbuddy;

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

        int id = getIntent().getIntExtra("HAIRSTYLE_ID", -1);
        Hairstyle h = HairstyleRepository.getById(id);
        if (h == null) { finish(); return; }

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
        tvMaintenance.setText("Maintenance: " + h.getMaintenanceLevel());
        tvTips.setText(h.getMaintenanceTips());
        tvTrend.setText(h.getTrend());
        tvSuitableFor.setText("Best for: " + String.join(", ", h.getSuitableFaceShapes()));
    }
}