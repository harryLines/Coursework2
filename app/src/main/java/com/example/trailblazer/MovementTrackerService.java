package com.example.trailblazer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovementTrackerService extends Service implements StepDetector.StepListener {
    public static final String ACTION_DISTANCE_UPDATE = "com.example.coursework2.ACTION_DISTANCE_UPDATE";
    private static final long MIN_ROUTEPOINTS_UPDATE_INTERVAL = 60000;
    private static final long MIN_ELEVATION_UPDATE_INTERVAL = 30000;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "Movement Tracker Channel";
    private static final String SECOND_CHANNEL_ID = "Movement Tracker Channel 2";

    private List<SavedLocation> savedLocations;
    private static final String TAG = MovementTrackerService.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int movementType = -1;
    private Location lastLocation;
    private double totalDistance = 0.0;
    private int caloriesBurned = 0;
    private float weight = 0;
    private long startTimeMillis;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long elapsedMillis;
    SensorManager sensorManager;
    private StepDetector stepDetector;
    Sensor accelerometerSensor;
    private List<LatLng> routePoints;
    private List<Double> elevationData;
    private long lastLocationPointTime;
    private long lastElevationDataTime;
    private DatabaseManager databaseManager;
    private boolean notificationUpdated = false;

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
        timerHandler = new Handler(Looper.getMainLooper());
        // Remove the following line since stepDetector is now initialized before this
        // stepDetector = new StepDetector(this);
        routePoints = new ArrayList<>();
        elevationData = new ArrayList<>();
        // Retrieve the user's weight from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);
        weight = sharedPreferences.getFloat("weight", 0.0f);
        lastLocationPointTime = 0;
        lastElevationDataTime = 0;
        initTimerRunnable();
        startLocationUpdates();
        databaseManager = new DatabaseManager(this);
    }


    // MET calculation based on (https://www.omicsonline.org/articles-images/2157-7595-6-220-t003.html)
    private int calculateCaloriesBurned() {
        double MET;
        double elapsedTimeInHours = (double) elapsedMillis / 3600000;
        double currentSpeedKMPH = (totalDistance / 1000) / elapsedTimeInHours;

        Log.d("CALORIES CALC", "Elapsed Time (hours): " + elapsedTimeInHours);
        Log.d("CALORIES CALC", "Total Distance (km): " + (totalDistance / 1000));
        Log.d("CALORIES CALC", "Current Speed (KMPH): " + currentSpeedKMPH);

        if(movementType != Trip.MOVEMENT_CYCLE) {
            if (currentSpeedKMPH <= 2.7) {
                MET = 2.3;
            } else if (currentSpeedKMPH <= 4) {
                MET = 2.9;
            } else if (currentSpeedKMPH <= 4.8) {
                MET = 3.3;
            } else if (currentSpeedKMPH <= 5.5) {
                MET = 3.6;
            } else if (currentSpeedKMPH <= 7) {
                MET = 7;
            } else {
                MET = 8;
            }
        } else {
            if (currentSpeedKMPH <= 5.5) {
                MET = 3.5;
            } else if (currentSpeedKMPH <= 9.4) {
                MET = 5.8;
            } else if (currentSpeedKMPH <= 11.9) {
                MET = 6.8;
            } else if (currentSpeedKMPH <= 13.9) {
                MET = 8.0;
            } else if (currentSpeedKMPH <= 15.9) {
                MET = 10.0;
            } else {
                MET = 12.0;
            }
        }

        int caloriesBurned = (int) (MET * weight * elapsedTimeInHours);

        Log.d("CALORIES CALC", "MET: " + MET);
        Log.d("CALORIES CALC", "Weight: " + weight);
        Log.d("CALORIES CALC", "Calories Burned: " + caloriesBurned);

        return caloriesBurned;
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
        if(intent == null) {
            Log.d(TAG, "Intent is NULL");
            stopSelf(); // Stop the service if the intent is null
            return START_NOT_STICKY;
        }

        movementType = intent.getIntExtra("movementType", -1);
        if (movementType == -1) {
            Log.e(TAG, "Movement type not provided in the intent");
            stopSelf(); // Stop the service if movementType is not provided
            return START_NOT_STICKY;
        }

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

        // Create a new Trip instance with the required data
        Trip trip = new Trip(
                new Date(),  // You can replace this with the actual date
                0,           // You can replace this with the actual trip ID
                totalDistance,
                movementType,
                elapsedMillis / 1000,
                routePoints,elevationData,caloriesBurned);

        // Create a Data object with the necessary information
        Data inputData = new Data.Builder()
                .putLong("startTimeMillis", trip.getDate().getTime())
                .putDouble("totalDistance", trip.getDistance())
                .putInt("movementType", trip.getMovementType())
                .putLong("elapsedMillis", trip.getTimeInSeconds() * 1000)
                .putString("routePoints", new Gson().toJson(trip.getRoutePoints()))
                .putString("elevationData", new Gson().toJson(trip.getElevationData()))
                .putInt("caloriesBurned", trip.getCaloriesBurned())
                .build();

        // Call the insertTripHistoryInBackground method from DatabaseManager
        OneTimeWorkRequest saveTripWorker =
                new OneTimeWorkRequest.Builder(DatabaseTripInsertWorker.class)
                        .setInputData(inputData)
                        .build();

        // Enqueue the work request to the WorkManager
        WorkManager.getInstance().enqueue(saveTripWorker);
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

            // Check if enough time has passed since the last elevation data update
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastElevationDataTime >= MIN_ELEVATION_UPDATE_INTERVAL) {
                caloriesBurned = calculateCaloriesBurned();
                // Add the obtained elevation to the elevation data list
                addElevationForLocation(newLocation);
                lastElevationDataTime = currentTime;
            }

            // Check if enough time has passed since the last route point update
            if (currentTime - lastLocationPointTime >= MIN_ROUTEPOINTS_UPDATE_INTERVAL) {
                // Add the new LatLng point to the list
                routePoints.add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                lastLocationPointTime = currentTime;
            }

            Log.d(TAG, "Distance Traveled: " + totalDistance + " meters");
            sendMovementUpdateBroadcast(totalDistance, newLocation);
        }
    }

    private void addElevationForLocation(Location location) {
        ElevationFinder.getElevation(location.getLatitude(), location.getLatitude(), new ElevationFinder.ElevationCallback() {
            @Override
            public void onElevationReceived(double elevation) {
                elevationData.add(elevation);
                Log.d(TAG, "Elevation: " + elevation + " meters");
            }

            @Override
            public void onError(String errorMessage) {
                // Handle the error here
                Log.e("Elevation Callback", "Error: " + errorMessage);
            }
        });
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

    private void showNotification(String locationName) {
        // Create a notification channel for Android Oreo and higher
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), SECOND_CHANNEL_ID)
                .setSmallIcon(R.drawable.running_icon)
                .setContentTitle("Location Saved")
                .setContentText("You are near " + locationName)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(1, builder.build());
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
                // Set the saved location as entered to avoid showing the notification again

                if (!savedLocation.isEntered()) {
                    savedLocation.setEntered(true);
                    updateNotificationWithSavedLocation(savedLocation.getName());
                }

                // Set intent extras
                intent.putExtra("savedLocationName", savedLocation.getName());
                intent.putExtra("savedLocationReminders", savedLocation.getRemindersAsString());
                foundCloseLocation = true;
                notificationUpdated = false;
                break; // Stop checking once a close location is found
            } else if(!notificationUpdated){
                notificationUpdated = true;
                updateNotificationWithNoSavedLocation();
            }
        }

        // If no close location is found, set the savedLocationName to "NULL"
        if (!foundCloseLocation) {
            intent.putExtra("savedLocationName", "NULL");
            intent.putExtra("savedLocationReminders", "NULL"); // Set reminders to an empty string or handle accordingly
        }

        intent.putExtra("distance", distance);
        long seconds = elapsedMillis / 1000;
        intent.putExtra("trackingDuration", seconds);
        intent.putExtra("stepCount", stepDetector.getStepCount());
        Log.d("CARLOEIWS", String.valueOf(caloriesBurned));
        intent.putExtra("caloriesBurned", caloriesBurned);
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

    private void updateNotificationWithSavedLocation(String savedLocationName) {
        // Create a notification channel (for Android Oreo and higher)
        createNotificationChannel();

        // Create an intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragmentToShow", "Logging");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the updated notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Movement Tracker")
                .setContentText("Your trip is being tracked.")
                .setSmallIcon(R.drawable.log_tab_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Auto-cancel the notification when clicked
                .setContentIntent(notificationPendingIntent);

        // If a saved location is found, add information to the notification
        if (!savedLocationName.equals("NULL")) {
            notificationBuilder.setContentText("Your trip is being tracked near " + savedLocationName);
            // You can customize the notification further based on your requirements
            // For example, you can add reminders or other information from the saved location
        }

        Notification updatedNotification = notificationBuilder.build();

        // Update the existing notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, updatedNotification);
    }

    private void updateNotificationWithNoSavedLocation() {
        // Create a notification channel (for Android Oreo and higher)
        createNotificationChannel();

        // Create an intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragmentToShow", "Logging");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the updated notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Movement Tracker")
                .setContentText("Your trip is being tracked.")
                .setSmallIcon(R.drawable.log_tab_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Auto-cancel the notification when clicked
                .setContentIntent(notificationPendingIntent);

        notificationBuilder.setContentText("Your trip is being tracked");

        Notification updatedNotification = notificationBuilder.build();

        // Update the existing notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, updatedNotification);
    }
}
