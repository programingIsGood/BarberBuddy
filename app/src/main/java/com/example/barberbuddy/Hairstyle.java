package com.example.barberbuddy;

import java.util.List;

public class Hairstyle {
    private int id;
    private String name;
    private int imageRes;
    private List<String> suitableFaceShapes;
    private String description;
    private String maintenanceLevel;   // "Low" / "Medium" / "High"
    private String maintenanceTips;
    private String trend;

    public Hairstyle(int id, String name, int imageRes, List<String> suitableFaceShapes,
                     String description, String maintenanceLevel,
                     String maintenanceTips, String trend) {
        this.id = id;
        this.name = name;
        this.imageRes = imageRes;
        this.suitableFaceShapes = suitableFaceShapes;
        this.description = description;
        this.maintenanceLevel = maintenanceLevel;
        this.maintenanceTips = maintenanceTips;
        this.trend = trend;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
    public List<String> getSuitableFaceShapes() { return suitableFaceShapes; }
    public String getDescription() { return description; }
    public String getMaintenanceLevel() { return maintenanceLevel; }
    public String getMaintenanceTips() { return maintenanceTips; }
    public String getTrend() { return trend; }
}