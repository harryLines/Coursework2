package com.example.trailblazer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovementTrackerService extends Service implements StepDetector.StepListener {
    public static final String ACTION_DISTANCE_UPDATE = "com.example.coursework2.ACTION_DISTANCE_UPDATE";
    private static final long MIN_ROUTEPOINTS_UPDATE_INTERVAL = 60000;
    private static final long MIN_ELEVATION_UPDATE_INTERVAL = 30000;
    private static final int NOTIFICATION_ID = 1;
    private String savedLocationName = "NULL";
    private String previousSavedLocationName = "NULL";
    boolean foundCloseLocation = false;
    private static final String CHANNEL_ID = "Movement Tracker Channel";
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
    private Database database;
    private TripDao tripDao;
    private SavedLocationDao savedLocationDao;
    private static int currentWeather = -1;
    private static String currentImage;

    /**
     * Called when the service is first created. Initializes sensor management, step detection,
     * location updates, and other required variables.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        database = DatabaseManager.getInstance(getApplicationContext());

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
    }

    public static void updateImage(String image) {
        currentImage = image;
    }

    public static void updateWeather(int weather) {
        currentWeather = weather;
    }

    /**
     * Calculates the calories burned based on the Metabolic Equivalent of Task (MET) formula,
     * taking into account the movement type, speed, and elapsed time.
     *
     * MET calculation based on the research article:
     * {https://www.omicsonline.org/articles-images/2157-7595-6-220-t003.html}
     *
     * @return The estimated calories burned as an integer value.
     */
    private int calculateCaloriesBurned() {
        double MET;
        double elapsedTimeInHours = (double) elapsedMillis / 3600000;
        double currentSpeedKMPH = (totalDistance / 1000) / elapsedTimeInHours;

        if (movementType != Trip.MOVEMENT_CYCLE) {
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

        return (int) (MET * weight * elapsedTimeInHours);
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

    /**
     * Callback method invoked when a step is detected by the step detector. Broadcasts the updated
     * step count to inform the application about step-related changes.
     *
     * @param stepCount The number of steps detected.
     */
    @Override
    public void onStepDetected(int stepCount) {
        // Broadcast the updated step count if needed
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);
        intent.putExtra("stepCount", stepCount);
        sendBroadcast(intent);
    }

    /**
     * Updates the elapsed time during tracking and broadcasts the updated tracking duration if needed.
     */
    private void updateTimer() {
        elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long seconds = elapsedMillis / 1000;

        // You can broadcast the updated duration if needed
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);
        intent.putExtra("trackingDuration", seconds);
        sendBroadcast(intent);
    }

    /**
     * Called when the service is started with an intent. Initializes tracking parameters, starts
     * location updates, and creates a foreground notification.
     *
     * @param intent  The intent used to start the service.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return An integer representing how the service should continue running.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
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
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    /**
     * Called when the service is destroyed. Stops the foreground service, location updates, and
     * step detection. Saves the trip data to the database.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
        sensorManager.unregisterListener(stepDetector);
        saveTripToDatabase();
        currentImage = null;
    }

    /**
     * Saves the trip data to the local database, including distance, duration, route points, elevation,
     * calories burned, weather, and image information.
     */
    private void saveTripToDatabase() {
        // Create a new Trip instance with the required data
        Trip trip = new Trip(
                new Date(),
                totalDistance,
                movementType,
                elapsedMillis / 1000,
                routePoints, elevationData, caloriesBurned, currentWeather, currentImage);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            tripDao = database.tripDao();
            tripDao.addNewTrip(trip);
        });
    }

    /**
     * Initializes location updates, including the location client and location callback.
     */
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

    /**
     * Starts location updates if location permission is granted. Sets the update interval for
     * receiving location updates.
     */
    private void startLocationUpdates() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000); // Update interval in milliseconds
            locationRequest.setFastestInterval(1000); // Fastest update interval

            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        } else {
            Log.e(TAG, "Location permission not granted");
        }
    }

    /**
     * Stops location updates.
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Calculates and updates the total distance traveled, checks for elevation data updates,
     * and broadcasts movement updates.
     *
     * @param newLocation The new Location object representing the current location.
     */
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

            sendMovementUpdateBroadcast(totalDistance, newLocation);
        }
    }

    /**
     * Retrieves and adds elevation data for a given location using the ElevationFinder utility class.
     *
     * @param location The Location object for which elevation data needs to be obtained.
     */
    private void addElevationForLocation(Location location) {
        ElevationFinder.getElevation(location.getLatitude(), location.getLongitude(), new ElevationFinder.ElevationCallback() {
            /**
             * Callback method invoked when elevation data is successfully received.
             *
             * @param elevation The elevation data (in meters) for the provided location.
             */
            @Override
            public void onElevationReceived(double elevation) {
                elevationData.add(elevation);
            }

            /**
             * Callback method invoked when an error occurs while retrieving elevation data.
             *
             * @param errorMessage A message describing the error that occurred.
             */
            @Override
            public void onError(String errorMessage) {
                // Handle the error here
                Log.e("Elevation Callback", "Error: " + errorMessage);
            }
        });
    }

    /**
     * Calculates the distance between two locations using the Haversine formula.
     *
     * @param startLat Starting latitude.
     * @param startLng Starting longitude.
     * @param endLat   Ending latitude.
     * @param endLng   Ending longitude.
     * @return The calculated distance in meters.
     */
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

    /**
     * Sends a broadcast with movement-related data, including distance, saved location name, tracking
     * duration, step count, and calories burned.
     *
     * @param distance        The total distance traveled.
     * @param currentLocation The current location of the user.
     */
    private void sendMovementUpdateBroadcast(double distance, Location currentLocation) {
        Intent intent = new Intent(ACTION_DISTANCE_UPDATE);

        boolean locationNameChanged = false;

        for (SavedLocation savedLocation : savedLocations) {
            double savedLat = savedLocation.getLatLng().latitude;
            double savedLng = savedLocation.getLatLng().longitude;
            double distanceToSavedLocation = calculateHaversineDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    savedLat, savedLng);
            // Check proximity
            if (distanceToSavedLocation < 100) {
                if (!savedLocation.isEntered()) {
                    savedLocationName = savedLocation.getName();
                    savedLocation.setEntered(true);
                    locationNameChanged = true;
                    // Set intent extras
                    foundCloseLocation = true;
                    break;
                }
                intent.putExtra("savedLocationReminders", savedLocation.getRemindersAsString());
            } else {
                foundCloseLocation = false;
            }
        }

        if (!foundCloseLocation) {
            savedLocationName = "NULL";
        }

        if (!savedLocationName.equals(previousSavedLocationName) || locationNameChanged) {
            // Update the notification only if the savedLocationName has changed or if it was just set
            updateNotification();
            previousSavedLocationName = savedLocationName;
        }

        // Set intent extras with the final savedLocationName value
        intent.putExtra("savedLocationName", savedLocationName);
        intent.putExtra("distance", distance);
        long seconds = elapsedMillis / 1000;
        intent.putExtra("trackingDuration", seconds);
        intent.putExtra("stepCount", stepDetector.getStepCount());
        intent.putExtra("caloriesBurned", caloriesBurned);
        sendBroadcast(intent);
    }

    /**
     * Updates the notification based on the current saved location name and other trip information.
     */
    private void updateNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, buildNotification());
    }

    /**
     * Loads saved locations from the database and initializes the savedLocations list.
     *
     * @return A list of SavedLocation objects loaded from the database.
     */
    private List<SavedLocation> loadSavedLocations() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            savedLocationDao = database.savedLocationDao();
            // Load saved locations from the database
            savedLocations = savedLocationDao.loadSavedLocations();
        });

        // Close the database connection
        return savedLocations;
    }

    /**
     * Creates a notification channel for displaying notifications related to the Movement Tracker.
     */
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

    /**
     * Builds and returns a notification for the foreground service.
     *
     * @return A NotificationCompat.Builder instance representing the foreground notification.
     */
    private Notification buildNotification() {
        // Create an intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragmentToShow", "Logging");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create an intent for the button
        Intent buttonIntent = new Intent(this, MainActivity.class);
        buttonIntent.putExtra("fragmentToShow", "N/A");
        buttonIntent.putExtra("stopLogging", true);
        buttonIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the updated notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Movement Tracker")
                .setContentText("Your trip is being tracked near " + savedLocationName + "\n Tap to view your reminders!")
                .setSmallIcon(R.drawable.log_tab_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Auto-cancel the notification when clicked
                .setContentIntent(notificationPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop Tracking", buttonPendingIntent);

        if("NULL".equals(savedLocationName)) {
            notificationBuilder.setContentText("Your trip is being tracked");
        }
        return notificationBuilder.build();
    }
}
