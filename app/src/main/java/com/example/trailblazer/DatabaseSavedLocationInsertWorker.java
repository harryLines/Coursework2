package com.example.trailblazer;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.maps.model.LatLng;

public class DatabaseSavedLocationInsertWorker extends Worker {
    private DatabaseManager dbManager;

    public DatabaseSavedLocationInsertWorker(
            @NonNull android.content.Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        dbManager = new DatabaseManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get input data
        String locationName = getInputData().getString("locationName");
        double latitude = getInputData().getDouble("latitude", 0.0);
        double longitude = getInputData().getDouble("longitude", 0.0);

        // Save the location to the database and get the locationId
        long locationId = saveLocationToDatabase(locationName, latitude, longitude);

        // Create an output Data object
        Data outputData = new Data.Builder()
                .putLong("locationId", locationId)
                .build();

        // Return success with output data
        return Result.success(outputData);
    }

    private long saveLocationToDatabase(String locationName, double latitude, double longitude) {
        // Save the location to the database and get the locationId
        return dbManager.saveLocation(locationName, new LatLng(latitude, longitude));
    }
}
