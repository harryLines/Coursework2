package com.example.coursework2;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Trip {
    private Date date;
    private long timeInSeconds;
    private int movementType;
    public static final int MOVEMENT_WALK = 0;
    public static final int MOVEMENT_RUN = 1;
    public static final int MOVEMENT_CYCLE = 2;
    private double distance;
    public Trip(Date date, double distance,int movementType, long time) {
        this.date = date;
        this.movementType = movementType;
        this.distance = distance;
        this.timeInSeconds = time;
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
}
