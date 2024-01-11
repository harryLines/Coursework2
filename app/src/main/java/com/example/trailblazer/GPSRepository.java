package com.example.trailblazer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GPSRepository {
    private MovementUpdateListener movementUpdateListener;
    private final Context applicationContext;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    public double totalDistance = 0.0;
    private long lastLocationPointTime;
    private long lastElevationDataTime;
    public List<LatLng> routePoints;
    private static final long MIN_ROUTEPOINTS_UPDATE_INTERVAL = 60000;
    private static final long MIN_ELEVATION_UPDATE_INTERVAL = 30000;
    @Inject
    public GPSRepository(@ApplicationContext Context context) {
        this.applicationContext = context.getApplicationContext();
        this.lastLocationPointTime = 0;
        this.lastElevationDataTime = 0;
    }
    public void initLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);

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
    public void startLocationUpdates() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000)
                    .build();

            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        } else {
            Log.e("PERMISSIONS", "Location permission not granted");
        }
    }
    /**
     * Stops location updates.
     */
    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Calculates and updates the total distance traveled, checks for elevation data updates,
     * and broadcasts movement updates.
     *
     * @param newLocation The new Location object representing the current location.
     */
    public void calculateDistance(Location newLocation) {
        if (lastLocation != null) {
            double distance = calculateHaversineDistance(
                    lastLocation.getLatitude(), lastLocation.getLongitude(),
                    newLocation.getLatitude(), newLocation.getLongitude());

            totalDistance += distance;

            // Check if enough time has passed since the last elevation data update
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastElevationDataTime >= MIN_ELEVATION_UPDATE_INTERVAL) {
                // Add the obtained elevation to the elevation data list
                lastElevationDataTime = currentTime;
            }

            // Check if enough time has passed since the last route point update
            if (currentTime - lastLocationPointTime >= MIN_ROUTEPOINTS_UPDATE_INTERVAL) {
                // Add the new LatLng point to the list
                routePoints.add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                lastLocationPointTime = currentTime;
            }

            if (movementUpdateListener != null) {
                movementUpdateListener.onMovementUpdate(totalDistance, newLocation);
            }
        }
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
    public double calculateHaversineDistance(double startLat, double startLng, double endLat, double endLng) {
        double R = 6371;

        double dLat = Math.toRadians(endLat - startLat);
        double dLng = Math.toRadians(endLng - startLng);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000;
    }


    public void setMovementUpdateListener(MovementUpdateListener listener) {
        this.movementUpdateListener = listener;
    }

    // Inside your calculateDistance or other relevant methods
}
