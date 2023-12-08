package com.example.coursework2;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MovementTrackerService extends Service {
    public static final String ACTION_DISTANCE_UPDATE = "com.example.coursework2.ACTION_DISTANCE_UPDATE";
    private List<SavedLocation> savedLocations;
    private static final String TAG = MovementTrackerService.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int movementType = -1;
    private Location lastLocation;
    private double totalDistance = 0.0;
    private long startTimeMillis;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long elapsedMillis;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Location","Service Started");
        initLocationUpdates();
        savedLocations = loadSavedLocations();
        startTimeMillis = System.currentTimeMillis();
        timerHandler = new Handler();
        initTimerRunnable();
    }

    private void initTimerRunnable() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000); // Update every 1000 milliseconds (1 second)
            }
        };
    }

    private void updateTimer() {
        elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long seconds = elapsedMillis / 1000;

        Log.d(TAG, "Tracking Duration: " + seconds + " seconds");

        // You can broadcast the updated duration if needed
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);
        intent.putExtra("trackingDuration", seconds);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        movementType = intent.getIntExtra("movementType",-1);
        startTimeMillis = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveTripToFile();
        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void saveTripToFile() {
        try {
            if (lastLocation != null && movementType != -1) {
                // Get the current date and time
                String dateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                        .format(Calendar.getInstance().getTime());

                // Format the trip data
                String tripData = String.format(Locale.UK, "%s,%s,%.2f,%s",
                        movementType, dateTime, totalDistance,elapsedMillis/1000);

                // Get the app's internal files directory
                File directory = getApplicationContext().getFilesDir();

                // Create a file to save trip details
                File tripFile = new File(directory, "trip_history.txt");

                // If the file doesn't exist, create a new one
                if (!tripFile.exists()) {
                    tripFile.createNewFile();
                }

                // Append the trip data to the file
                FileWriter writer = new FileWriter(tripFile, true);
                writer.append(tripData).append("\n");
                writer.close();

                Log.d(TAG, "Trip details saved to file: " + tripFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Handle the location update, e.g., calculate distance
                    calculateDistance(location);
                    lastLocation = location;
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000); // Update interval in milliseconds
        locationRequest.setFastestInterval(1000); // Fastest update interval

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void calculateDistance(Location newLocation) {
        if (lastLocation != null) {
            double distance = calculateHaversineDistance(
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    newLocation.getLatitude(), newLocation.getLongitude());

            totalDistance += distance;

            Log.d(TAG, "Distance Traveled: " + totalDistance + " meters");
            sendDistanceAndTimerUpdateBroadcast(totalDistance,newLocation);
        }
    }

    private double calculateHaversineDistance(double startLat, double startLng, double endLat, double endLng) {
        double R = 6371; // Earth radius in kilometers

        double dLat = Math.toRadians(endLat - startLat);
        double dLng = Math.toRadians(endLng - startLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // Distance in meters
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendDistanceAndTimerUpdateBroadcast(double distance, Location currentLocation) {
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);

        // Check if the user is close to a saved location
        for (SavedLocation savedLocation : savedLocations) {
            double savedLat = savedLocation.getLatLng().latitude;
            double savedLng = savedLocation.getLatLng().longitude;

            double distanceToSavedLocation = calculateHaversineDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    savedLat, savedLng);
            Log.d("Distance", distanceToSavedLocation + "m away from " + savedLocation.getName());
            // You can adjust the radius (in meters) as needed
            if (distanceToSavedLocation < 25) {
                intent.putExtra("savedLocationName", savedLocation.getName());
                break; // Stop checking once a close location is found
            }
        }

        intent.putExtra("distance", distance);

        // Add tracking duration to the intent
        long seconds = elapsedMillis / 1000;
        intent.putExtra("trackingDuration", seconds);

        sendBroadcast(intent);
    }


    private List<SavedLocation> loadSavedLocations() {
        List<SavedLocation> savedLocations = new ArrayList<>();

        try {
            File file = new File(getApplicationContext().getFilesDir(), "saved_locations.txt");

            if (!file.exists()) {
                Log.d("FILE","File Doesn't exist");
                return null;
            } else {

                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // Split the line using the delimiter
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String name = parts[0].trim();
                        double latitude = Double.parseDouble(parts[1].trim());
                        double longitude = Double.parseDouble(parts[2].trim());

                        LatLng latLng = new LatLng(latitude, longitude);
                        SavedLocation savedLocation = new SavedLocation(name, latLng);
                        savedLocations.add(savedLocation);
                    }
                }

                bufferedReader.close();
            }
            } catch (IOException e){
                e.printStackTrace();
            }
        return savedLocations;
    }
}
