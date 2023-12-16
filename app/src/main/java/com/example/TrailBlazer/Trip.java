package com.example.TrailBlazer;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

public class Trip {
    private Date date;
    private long timeInSeconds;
    private long tripID;
    private int movementType;
    public static final int MOVEMENT_WALK = 0;
    public static final int MOVEMENT_RUN = 1;
    public static final int MOVEMENT_CYCLE = 2;
    private double distance;
    List<LatLng> route;
    public Trip(Date date, long tripID, double distance, int movementType, long time, List<LatLng> routePoints) {
        this.date = date;
        this.tripID = tripID;
        this.movementType = movementType;
        this.distance = distance;
        this.timeInSeconds = time;
        this.route = routePoints;
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
}
