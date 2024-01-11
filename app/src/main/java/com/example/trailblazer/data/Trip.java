package com.example.trailblazer.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;
@Entity(tableName = "trip_history")
@TypeConverters({Converters.class})
public class Trip {
    public static final int MOVEMENT_WALK = 0;
    public static final int MOVEMENT_RUN = 1;
    public static final int MOVEMENT_CYCLE = 2;
    public static final int WEATHER_SUNNY = 0;
    public static final int WEATHER_RAINY = 1;
    public static final int WEATHER_SNOW = 2;
    public static final int WEATHER_THUNDERSTORM = 3;
    public static final int WEATHER_FOGGY = 4;
    public static final int WEATHER_WINDY = 5;
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long tripID;
    @ColumnInfo(name = "date")
    private Date date;
    @ColumnInfo(name = "time")
    private long timeInSeconds;
    @ColumnInfo(name = "movement_type")
    private int movementType;
    @ColumnInfo(name = "distance_traveled")
    private double distance;
    @ColumnInfo(name = "calories_burned")
    private int caloriesBurned;
    @ColumnInfo(name = "elevation_data")
    private List<Double> elevationData;
    @ColumnInfo(name = "route_points")
    private List<LatLng> routePoints;
    @ColumnInfo(name = "weather")
    private int weather;
    @ColumnInfo(name = "image")
    private String image;

    public long getTripID() {
        return tripID;
    }
    public void setTripID(long tripID) {
        this.tripID = tripID;
    }
    public int getWeather() {
        return weather;
    }
    public void setWeather(int weather) {
        this.weather = weather;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
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
        return routePoints;
    }
    public void setRoutePoints(List<LatLng> routePoints) {
        this.routePoints = routePoints;
    }

    public List<Double> getElevationData() {
        return elevationData;
    }

    public void setElevationData(List<Double> elevationData) {
        this.elevationData = elevationData;
    }
    public Trip(Date date, double distance, int movementType, long time, List<LatLng> routePoints, List<Double> elevationData, int calories, int weather, String image) {
        this.date = date;
        this.movementType = movementType;
        this.distance = distance;
        this.timeInSeconds = time;
        this.routePoints = routePoints;
        this.elevationData = elevationData;
        this.caloriesBurned = calories;
        this.weather = weather;
        this.image = image;
    }

    public Trip() {

    }
}
