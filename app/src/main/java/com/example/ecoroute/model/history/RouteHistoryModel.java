package com.example.ecoroute.model.history;

public class RouteHistoryModel {
    private final String startLocation;
    private final String destination;
    private final String status;
    private final String date;
    private final String distance;

    public RouteHistoryModel(String startLocation, String destination, String status, String date, String distance) {
        this.startLocation = startLocation;
        this.destination = destination;
        this.status = status;
        this.date = date;
        this.distance = distance;
    }

    public String getStartLocation() { return startLocation; }
    public String getDestination() { return destination; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getDistance() { return distance; }

}
