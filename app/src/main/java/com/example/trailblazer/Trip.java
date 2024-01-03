package com.example.trailblazer;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

public class Trip {
    private Date date;
    private long timeInSeconds;
    private int movementType;
    public static final int MOVEMENT_WALK = 0;
    public static final int MOVEMENT_RUN = 1;
    public static final int MOVEMENT_CYCLE = 2;
    private double distance;
    private int caloriesBurned;
    private List<Double> elevationData;
    final List<LatLng> route;

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public Trip(Date date, long tripID, double distance, int movementType, long time, List<LatLng> routePoints, List<Double> elevationData, int calories) {
        this.date = date;
        this.movementType = movementType;
        this.distance = distance;
        this.timeInSeconds = time;
        this.route = routePoints;
        this.elevationData = elevationData;
        this.caloriesBurned = calories;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMovementType() {
        return movementType;
    }

    public void setMovementType(int movementType) {
        this.movementType = movementType;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public List<LatLng> getRoutePoints() {
        return route;
    }

    public List<Double> getElevationData() {
        return elevationData;
    }

    public void setElevationData(List<Double> elevationData) {
        this.elevationData = elevationData;
    }
}
