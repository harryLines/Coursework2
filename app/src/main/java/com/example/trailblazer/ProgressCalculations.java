package com.example.trailblazer;

import java.util.List;
import java.util.Locale;

public class ProgressCalculations {


    /**
     * Formats a distance value to a human-readable string with two decimal places and " km" appended.
     *
     * @param distance The distance value in meters.
     * @return The formatted distance string.
     */
    public static String formatDistance(double distance) {
        // Format distance with 2 decimal points and append " km"
        return String.format(Locale.getDefault(), "%.2f km", distance/1000);
    }

    /**
     * Formats a speed value to a human-readable string with one decimal place and " km/h" appended.
     *
     * @param speed The speed value in kilometers per hour (km/h).
     * @return The formatted speed string.
     */
    public static String formatSpeed(double speed) {
        // Format speed to one decimal place and append " m/s"
        return String.format(Locale.getDefault(), "%.1f km/h", speed);
    }

    /**
     * Formats a time value in seconds to a human-readable string in hours, minutes, and seconds format.
     *
     * @param timeInSeconds The time value in seconds.
     * @return The formatted time string.
     */
    public static String formatTime(long timeInSeconds) {
        // Format time in hours, minutes, and seconds
        long hours = timeInSeconds / 3600;
        long minutes = (timeInSeconds % 3600) / 60;
        long seconds = timeInSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    /**
     * Formats a percentage change value to a human-readable string with a sign.
     *
     * @param percentChangeTime The percentage change value to format.
     * @return The formatted percentage change string.
     */
    public static String formatTimeChange(int percentChangeTime) {
        // Format time change percentage with a sign
        return String.format(Locale.getDefault(), "%s%d%%", (percentChangeTime >= 0) ? "+" : "", percentChangeTime);
    }

    /**
     * Calculates the total distance traveled for a given movement type.
     *
     * @param trips        The list of trips to calculate the total distance from.
     * @param movementType The movement type (e.g., walking, running, cycling).
     * @return The total distance traveled for the specified movement type in meters.
     */
    public static double calculateTotalDistance(List<Trip> trips, int movementType) {
        double totalDistance = 0;
        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                totalDistance += trip.getDistance();
            }
        }
        return totalDistance;
    }

    /**
     * Calculates the average speed for a given movement type.
     *
     * @param trips        The list of trips to calculate the average speed from.
     * @param movementType The movement type (e.g., walking, running, cycling).
     * @return The average speed for the specified movement type in kilometers per hour (km/h).
     */
    public static double calculateAverageSpeed(List<Trip> trips, int movementType) {
        double totalSpeed = 0;
        int count = 0;

        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                // Convert meters to kilometers and seconds to hours
                double distanceKm = trip.getDistance() / 1000.0; // Convert meters to kilometers
                double timeHours = trip.getTimeInSeconds() / 3600.0; // Convert seconds to hours

                // Calculate speed in km/h
                double speedKmph = distanceKm / timeHours;

                totalSpeed += speedKmph;
                count++;
            }
        }

        if (count > 0) {
            return totalSpeed / count;
        } else {
            return 0;
        }
    }

    /**
     * Calculates the total time spent for a given movement type.
     *
     * @param trips        The list of trips to calculate the total time from.
     * @param movementType The movement type (e.g., walking, running, cycling).
     * @return The total time spent for the specified movement type in seconds.
     */
    public static long calculateTotalTime(List<Trip> trips, int movementType) {
        long totalTime = 0;
        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                totalTime += trip.getTimeInSeconds();
            }
        }
        return totalTime;
    }

    /**
     * Calculates the percentage change between current and previous values.
     *
     * @param currentValue  The current value.
     * @param previousValue The previous value.
     * @return The percentage change between the current and previous values.
     */
    public static int calculatePercentageChange(double currentValue, double previousValue) {
        if (previousValue != 0) {
            double percentageChange = ((currentValue - previousValue) / Math.abs(previousValue)) * 100;
            return (int) percentageChange;
        } else {
            return 0; // Handle division by zero or when there's no previous value
        }
    }

}
