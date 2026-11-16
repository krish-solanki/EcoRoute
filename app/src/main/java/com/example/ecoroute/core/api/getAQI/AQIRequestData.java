package com.example.ecoroute.core.api.getAQI;

public class AQIRequestData {
    private final double latitude;
    private final double longitude;

    public AQIRequestData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
