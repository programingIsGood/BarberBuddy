package com.example.barberbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HairstyleAdapter extends RecyclerView.Adapter<HairstyleAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Hairstyle hairstyle);
    }

    private final List<Hairstyle> hairstyles;
    private final OnItemClickListener listener;

    public HairstyleAdapter(List<Hairstyle> hairstyles, OnItemClickListener listener) {
        this.hairstyles = hairstyles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hairstyle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Hairstyle h = hairstyles.get(position);
        Context context = holder.itemView.getContext();

        // ------------------------
        // TEXT DATA
        // ------------------------
        holder.tvName.setText(h.getName());
        holder.tvTrend.setText(h.getTrend());
        holder.tvMaintenance.setText(h.getMaintenanceLevel() + " maintenance");

        // ------------------------
        // IMAGE
        // ------------------------
        Glide.with(holder.itemView)
                .load(h.getImageRes())
                .centerCrop()
                .into(holder.imgStyle);

        // ------------------------
        // SAVE ICON STATE
        // ------------------------
        boolean saved = SavedStylesManager.isSaved(context, h.getId());

        holder.ivSave.setImageResource(
                saved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline
        );

        // ------------------------
        // SAVE / UNSAVE CLICK
        // ------------------------
        holder.ivSave.setOnClickListener(v -> {

            boolean isSaved = SavedStylesManager.isSaved(context, h.getId());

            if (isSaved) {
                SavedStylesManager.remove(context, h.getId());
                holder.ivSave.setImageResource(R.drawable.ic_bookmark_outline);
            } else {
                SavedStylesManager.save(context, h.getId());
                holder.ivSave.setImageResource(R.drawable.ic_bookmark_filled);
            }
        });

        // ------------------------
        // ITEM CLICK (DETAIL PAGE)
        // ------------------------
        holder.itemView.setOnClickListener(v -> listener.onItemClick(h));
    }

    @Override
    public int getItemCount() {
        return hairstyles.size();
    }

    // ------------------------
    // VIEW HOLDER
    // ------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgStyle;
        ImageView ivSave;
        TextView tvName, tvTrend, tvMaintenance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgStyle = itemView.findViewById(R.id.imgStyle);
            ivSave = itemView.findViewById(R.id.ivSave);
            tvName = itemView.findViewById(R.id.tvStyleName);
            tvTrend = itemView.findViewById(R.id.tvTrend);
            tvMaintenance = itemView.findViewById(R.id.tvMaintenance);
        }
    }
}