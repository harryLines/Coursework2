package com.example.trailblazer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        int weather = getInputData().getInt("weather",-1);
        String image = getInputData().getString("image");

        // Create a new Trip instance with the required data
        Trip trip = new Trip(new Date(startTimeMillis), 0, totalDistance, movementType, elapsedMillis / 1000, routePoints,elevationData,caloriesBurned,weather,image);

        // Initialize DatabaseManager and insert the trip into the database
        dbManager.insertTripHistory(trip);
        return Result.success();
    }

    private byte[] convertImagePathToByteArray(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null;
        }
    }
}

