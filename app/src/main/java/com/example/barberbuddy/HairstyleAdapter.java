package com.example.barberbuddy;

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
        holder.tvName.setText(h.getName());
        holder.tvTrend.setText(h.getTrend());
        holder.tvMaintenance.setText(h.getMaintenanceLevel() + " maintenance");

        Glide.with(holder.itemView)
                .load(h.getImageRes())
                .centerCrop()
                .into(holder.imgStyle);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(h));
    }

    @Override
    public int getItemCount() { return hairstyles.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStyle;
        TextView tvName, tvTrend, tvMaintenance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStyle      = itemView.findViewById(R.id.imgStyle);
            tvName        = itemView.findViewById(R.id.tvStyleName);
            tvTrend       = itemView.findViewById(R.id.tvTrend);
            tvMaintenance = itemView.findViewById(R.id.tvMaintenance);
        }
    }
}