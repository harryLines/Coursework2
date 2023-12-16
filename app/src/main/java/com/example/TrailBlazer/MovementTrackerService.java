package com.example.TrailBlazer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MovementTrackerService extends Service implements StepDetector.StepListener {
    public static final String ACTION_DISTANCE_UPDATE = "com.example.coursework2.ACTION_DISTANCE_UPDATE";
    private static final long MIN_LOCATION_UPDATE_INTERVAL = 60000;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "Movement Tracker Channel";
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
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
    SensorManager sensorManager;
    private StepDetector stepDetector;
    Sensor accelerometerSensor;
    private List<LatLng> routePoints;
    private long lastLocationPointTime;
    private DatabaseManager databaseManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Location SRVC", "Service Started");

        // Initialize sensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Log.e(TAG, "SensorManager is not available on this device");
            // Handle the case when the SensorManager is not available
            stopSelf(); // Stop the service if SensorManager is not available
            return;
        }

        // Use the accelerometer sensor
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor == null) {
            // Handle the case when the accelerometer sensor is not available on this device
            Log.e(TAG, "Accelerometer sensor is not available on this device");
            // You may stop the service or handle it according to your needs
            stopSelf();
            return;
        }

        // Initialize stepDetector before registering as a listener
        stepDetector = new StepDetector(this);

        // Register the sensor event listener
        sensorManager.registerListener(stepDetector, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        initLocationUpdates();
        savedLocations = loadSavedLocations();
        startTimeMillis = System.currentTimeMillis();
        timerHandler = new Handler();
        // Remove the following line since stepDetector is now initialized before this
        // stepDetector = new StepDetector(this);
        routePoints = new ArrayList<>();
        lastLocationPointTime = 0;
        initTimerRunnable();
        databaseManager = new DatabaseManager(this);
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

    @Override
    public void onStepDetected(int stepCount) {
        // This method is called when a step is detected
        // You can perform actions based on the step count
        Log.d(TAG, "Step count: " + stepCount);

        // Broadcast the updated step count if needed
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);
        intent.putExtra("stepCount", stepCount);
        sendBroadcast(intent);
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
        movementType = intent.getIntExtra("movementType", -1);
        startTimeMillis = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        startForeground(NOTIFICATION_ID, buildForegroundNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        saveTripToDatabase();
        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener(stepDetector);
        databaseManager.close();
    }

    private void saveTripToDatabase() {
        SQLiteDatabase db = databaseManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.COLUMN_MOVEMENT_TYPE, movementType);
        values.put(DatabaseManager.COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                .format(Calendar.getInstance().getTime()));
        values.put(DatabaseManager.COLUMN_DISTANCE_TRAVELED, totalDistance);
        values.put(DatabaseManager.COLUMN_TIME,elapsedMillis / 1000);
        String routePointsJson = new Gson().toJson(routePoints);
        values.put(DatabaseManager.COLUMN_ROUTE_POINTS, routePointsJson);

        // Insert the data into the database
        long rowId = db.insert(DatabaseManager.TABLE_TRIP_HISTORY, null, values);

        db.close();
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
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000); // Update interval in milliseconds
            locationRequest.setFastestInterval(1000); // Fastest update interval

            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        } else {
            Log.e(TAG, "Location permission not granted");
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

            // Check if enough time has passed since the last route point
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLocationPointTime >= MIN_LOCATION_UPDATE_INTERVAL) {
                // Add the new LatLng point to the list
                routePoints.add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                lastLocationPointTime = currentTime;
            }

            Log.d(TAG, "Distance Traveled: " + totalDistance + " meters");
            sendMovementUpdateBroadcast(totalDistance, newLocation);
        }
    }
    // Use a formula to calculate distance instead of outsourcing to an API to ensure speed
    private double calculateHaversineDistance(double startLat, double startLng, double endLat, double endLng) {
        double R = 6371;

        double dLat = Math.toRadians(endLat - startLat);
        double dLng = Math.toRadians(endLng - startLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendMovementUpdateBroadcast(double distance, Location currentLocation) {
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);

        boolean foundCloseLocation = false;

        // Check if the user is close to a saved location
        for (SavedLocation savedLocation : savedLocations) {
            double savedLat = savedLocation.getLatLng().latitude;
            double savedLng = savedLocation.getLatLng().longitude;

            double distanceToSavedLocation = calculateHaversineDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    savedLat, savedLng);

            Log.d("Distance", distanceToSavedLocation + "m away from " + savedLocation.getName());

            // You can adjust the radius (in meters) as needed
            if (distanceToSavedLocation < 100) {
                intent.putExtra("savedLocationName", savedLocation.getName());
                intent.putExtra("savedLocationReminders", savedLocation.getRemindersAsString());
                foundCloseLocation = true;
                break; // Stop checking once a close location is found
            }
        }

        // If no close location is found, set the savedLocationName to "NULL"
        if (!foundCloseLocation) {
            intent.putExtra("savedLocationName", "NULL");
            intent.putExtra("savedLocationReminders", "NULL"); // Set reminders to an empty string or handle accordingly
        }

        intent.putExtra("distance", distance);

        // Add tracking duration to the intent
        long seconds = elapsedMillis / 1000;
        intent.putExtra("trackingDuration", seconds);

        intent.putExtra("stepCount", stepDetector.getStepCount());
        sendBroadcast(intent);
    }

    private List<SavedLocation> loadSavedLocations() {
        // Initialize your DatabaseManager
        DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());

        // Load saved locations from the database
        List<SavedLocation> savedLocations = databaseManager.loadSavedLocations();

        // Close the database connection
        databaseManager.close();

        return savedLocations;
    }

    private Notification buildForegroundNotification() {
        // Create a notification channel (for Android Oreo and higher)
        createNotificationChannel();

        // Create an intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragmentToShow", "Logging");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create an intent for the button
        Intent buttonIntent = new Intent(this, MainActivity.class);  // Replace YourButtonActionActivity with the actual activity you want to open on button click
        buttonIntent.putExtra("fragmentToShow", "N/A");
        buttonIntent.putExtra("stopLogging", true);
        buttonIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the foreground notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Movement Tracker")
                .setContentText("Your trip is being tracked.")
                .setSmallIcon(R.drawable.log_tab_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Auto-cancel the notification when clicked
                .setContentIntent(notificationPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop Tracking", buttonPendingIntent);

        return notificationBuilder.build();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Movement Tracker Channel";
            String description = "Channel for Movement Tracker notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}