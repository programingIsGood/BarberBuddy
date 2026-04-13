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

    public Hairstyle(int id, String name, int imageRes, int overlayRes,
                     List<String> suitableFaceShapes, String description,
                     String maintenanceLevel, String maintenanceTips,
                     String trend, String modelPath) {
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
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
    public int getOverlayRes() { return overlayRes; }
    public List<String> getSuitableFaceShapes() { return suitableFaceShapes; }
    public String getDescription() { return description; }
    public String getMaintenanceLevel() { return maintenanceLevel; }
    public String getMaintenanceTips() { return maintenanceTips; }
    public String getTrend() { return trend; }
    public String getModelPath() { return modelPath; }
}
