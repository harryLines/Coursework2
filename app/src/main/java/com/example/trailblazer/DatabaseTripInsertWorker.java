package com.example.trailblazer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;


public class DatabaseTripInsertWorker extends Worker {
    private final DatabaseManager dbManager;
    public DatabaseTripInsertWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.dbManager = DatabaseManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve input data
        long startTimeMillis = getInputData().getLong("startTimeMillis", 0);
        double totalDistance = getInputData().getDouble("totalDistance", 0.0);
        int movementType = getInputData().getInt("movementType", -1);
        long elapsedMillis = getInputData().getLong("elapsedMillis", 0);
        String routePointsJson = getInputData().getString("routePoints");
        String elevationDataJson = getInputData().getString("elevationData");
        List<LatLng> routePoints = new Gson().fromJson(routePointsJson, new TypeToken<List<LatLng>>(){}.getType());
        List<Double> elevationData = new Gson().fromJson(elevationDataJson, new TypeToken<List<Double>>(){}.getType());
        int caloriesBurned = getInputData().getInt("caloriesBurned",0);

        // Create a new Trip instance with the required data
        Trip trip = new Trip(new Date(startTimeMillis), 0, totalDistance, movementType, elapsedMillis / 1000, routePoints,elevationData,caloriesBurned);

        // Initialize DatabaseManager and insert the trip into the database
        dbManager.insertTripHistory(trip);
        return Result.success();
    }
}

