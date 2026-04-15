package com.example.barberbuddy;

import java.util.List;

public class Hairstyle {

    private int id;
    private String name;

    private int imageRes;
    private int overlayRes;

    private List<String> suitableFaceShapes;

    private String description;
    private String maintenanceLevel;
    private String maintenanceTips;

    private String trend;
    private String modelPath;

    private String category;

    private boolean asianStyle;

    // ─────────────────────────────
    // MAIN CONSTRUCTOR (UPDATED)
    // ─────────────────────────────
    public Hairstyle(int id,
                     String name,
                     int imageRes,
                     int overlayRes,
                     List<String> suitableFaceShapes,
                     String description,
                     String maintenanceLevel,
                     String maintenanceTips,
                     String trend,
                     String modelPath,
                     boolean asianStyle) {

        this.id = id;
        this.name = name;
        this.imageRes = imageRes;
        this.overlayRes = overlayRes;
        this.suitableFaceShapes = suitableFaceShapes;
        this.description = description;
        this.maintenanceLevel = maintenanceLevel;
        this.maintenanceTips = maintenanceTips;
        this.trend = trend;
        this.modelPath = modelPath;

        this.asianStyle = asianStyle;

        // simple fallback (you can improve later)
        this.category = trend;
    }

    // ─────────────────────────────
    // GETTERS
    // ─────────────────────────────

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getImageRes() {
        return imageRes;
    }

    public int getOverlayRes() {
        return overlayRes;
    }

    public List<String> getSuitableFaceShapes() {
        return suitableFaceShapes;
    }

    public String getDescription() {
        return description;
    }

    public String getMaintenanceLevel() {
        return maintenanceLevel;
    }

    public String getMaintenanceTips() {
        return maintenanceTips;
    }

    public String getTrend() {
        return trend;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAsianStyle() {
        return asianStyle;
    }

    // ─────────────────────────────
    // OPTIONAL SETTERS (future use)
    // ─────────────────────────────

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAsianStyle(boolean asianStyle) {
        this.asianStyle = asianStyle;
    }
}