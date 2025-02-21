package edu.suresh.mealmate.model;

import java.util.List;

public class SavedLocation {
    private String name;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private String distance;
    private List<String> availableIngredients;
    private int matchingCount;

    public SavedLocation(String name, String imageUrl, double latitude, double longitude, String distance, List<String> availableIngredients, int matchingCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.availableIngredients = availableIngredients;
        this.matchingCount = matchingCount;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }

    public List<String> getAvailableIngredients() {
        return availableIngredients;
    }

    public int getMatchingCount() {
        return matchingCount;
    }
}
