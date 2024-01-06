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
    public static final int WEATHER_SUNNY = 0;
    public static final int WEATHER_RAINY = 1;
    public static final int WEATHER_SNOW = 2;
    public static final int WEATHER_THUNDERSTORM = 3;
    public static final int WEATHER_FOGGY = 4;
    public static final int WEATHER_WINDY = 5;
    private double distance;
    private int caloriesBurned;
    private List<Double> elevationData;
    final List<LatLng> route;


    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    private int weather;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String image;

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public Trip(Date date, long tripID, double distance, int movementType, long time, List<LatLng> routePoints, List<Double> elevationData, int calories, int weather, String image) {
        this.date = date;
        this.movementType = movementType;
        this.distance = distance;
        this.timeInSeconds = time;
        this.route = routePoints;
        this.elevationData = elevationData;
        this.caloriesBurned = calories;
        this.weather = weather;
        this.image = image;
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
