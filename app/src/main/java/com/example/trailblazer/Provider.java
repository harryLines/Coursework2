package com.example.trailblazer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Provider extends ContentProvider {
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int REMINDERS = 100;
    private static final int TRIPS = 102;
    private static final int GOALS = 104;
    private static final int SAVED_LOCATIONS = 106;
    private Database database;

    static {
        sUriMatcher.addURI(Contract.AUTHORITY, "reminders", REMINDERS); //WORKS
        sUriMatcher.addURI(Contract.AUTHORITY, "goals", GOALS); //WORKS
        sUriMatcher.addURI(Contract.AUTHORITY, "saved_locations", SAVED_LOCATIONS); //WORKS
        sUriMatcher.addURI(Contract.AUTHORITY, "trip_history", TRIPS);
    }

    @Override
    public boolean onCreate() {
        database = DatabaseManager.getInstance(requireContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        MatrixCursor cursor;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case GOALS:
                cursor = new MatrixCursor(new String[]{"metric_type","number_of_timeframes","timeframe_type","progress","target","date_created","is_complete"});
                List<Goal> goals = database.goalDao().loadGoals(); // Implement this method according to your data source.
                for (Goal goal : goals) {
                    cursor.addRow(new Object[]{goal.getMetricType(), goal.getNumberOfTimeframes(), goal.getTimeframeType(), goal.getProgress(), goal.getTarget(), goal.getDateCreated(), goal.isComplete});
                }
                return cursor;
            case TRIPS:
                cursor = new MatrixCursor(new String[]{"date","time","movement_type","distance_traveled","calories_burned","elevation_data","route_points","weather","image"});
                List<Trip> trips = database.tripDao().loadTripHistory();
                for(Trip trip : trips) {
                    cursor.addRow(new Object[]{trip.getDate(),trip.getTimeInSeconds(),trip.getMovementType(),trip.getDistance(),trip.getCaloriesBurned(),trip.getElevationData(),trip.getRoutePoints(),trip.getWeather(),trip.getImage()});
                }
                return cursor;
            case SAVED_LOCATIONS:
                cursor = new MatrixCursor(new String[]{"name","latlng","reminders"});
                List<SavedLocation> savedLocations = database.savedLocationDao().loadSavedLocations();
                for(SavedLocation location : savedLocations) {
                    cursor.addRow(new Object[]{location.getName(),location.getLatLng(),location.getReminders()});
                }
                return cursor;
            case REMINDERS:
                cursor = new MatrixCursor(new String[]{"location_id", "reminder_text"});
                List<Reminder> reminders = database.reminderDao().loadReminders();
                for (Reminder reminder : reminders) {
                    cursor.addRow(new Object[]{reminder.getLocationID(), reminder.getReminderText()});
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case REMINDERS:
                long locationID;
                String reminderText;
                assert contentValues != null;
                if (contentValues.containsKey("location_id")) {
                    locationID = contentValues.getAsLong("location_id");
                } else {
                    return null;
                }
                if (contentValues.containsKey("reminder_text")) {
                    reminderText = contentValues.getAsString("reminder_text");
                } else {
                    return null;
                }
                // Assuming you have a method in your DAO to insert a reminder
                Reminder newReminder = new Reminder(locationID,reminderText);
                long reminderId = database.reminderDao().addNewReminder(newReminder);
                if (reminderId > 0) {
                    returnUri = ContentUris.withAppendedId(uri, reminderId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case GOALS:
                int metricType;
                int numberOfTimeframes;
                int timeframeType;
                double progress;
                double target;
                Date dateCreated;
                boolean isComplete;

                assert contentValues != null;
                if (contentValues.containsKey("metric_type")) {
                    metricType = contentValues.getAsInteger("metric_type");
                } else {
                    return null;
                }
                if (contentValues.containsKey("number_of_timeframes")) {
                    numberOfTimeframes = contentValues.getAsInteger("number_of_timeframes");
                } else {
                    return null;
                }
                if (contentValues.containsKey("timeframe_type")) {
                    timeframeType = contentValues.getAsInteger("timeframe_type");
                } else {
                    return null;
                }
                if (contentValues.containsKey("progress")) {
                    progress = contentValues.getAsLong("progress");
                } else {
                    return null;
                }
                if (contentValues.containsKey("target")) {
                    target = contentValues.getAsLong("target");
                } else {
                    return null;
                }
                if (contentValues.containsKey("date_created")) {
                    dateCreated = Converters.fromTimestamp(contentValues.getAsLong("date_created"));
                } else {
                    return null;
                }
                if (contentValues.containsKey("is_complete")) {
                    isComplete = contentValues.getAsBoolean("is_complete");
                } else {
                    return null;
                }
                Goal newGoal = new Goal(metricType, numberOfTimeframes, timeframeType,progress,target,dateCreated,isComplete);
                long goalID = database.goalDao().addNewGoal(newGoal);
                if (goalID > 0) {
                    returnUri = ContentUris.withAppendedId(uri, goalID);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case TRIPS:
                Date date;
                int movementType;
                double distance;
                int timeInSeconds;
                List<LatLng> routePoints;
                List<Double> elevationData;
                int caloriesBurned;
                int weather;
                String image;

                assert contentValues != null;
                if (contentValues.containsKey("date")) {
                    date = Converters.fromTimestamp(contentValues.getAsLong("date"));
                } else {
                    return null;
                }
                if (contentValues.containsKey("time")) {
                    timeInSeconds = contentValues.getAsInteger("time");
                } else {
                    return null;
                }
                if (contentValues.containsKey("movement_type")) {
                    movementType = contentValues.getAsInteger("movement_type");
                } else {
                    return null;
                }
                if (contentValues.containsKey("distance_traveled")) {
                    distance = contentValues.getAsDouble("distance_traveled");
                } else {
                    return null;
                }
                if (contentValues.containsKey("calories_burned")) {
                    caloriesBurned = contentValues.getAsInteger("calories_burned");
                } else {
                    return null;
                }
                if (contentValues.containsKey("elevation_data")) {
                    elevationData = Converters.toDoubleList(contentValues.getAsString("elevation_data"));
                } else {
                    return null;
                }
                if (contentValues.containsKey("route_points")) {
                    routePoints = Converters.toLatLngList(contentValues.getAsString("route_points"));
                } else {
                    return null;
                }
                if (contentValues.containsKey("weather")) {
                    weather = contentValues.getAsInteger("weather");
                } else {
                    return null;
                }
                if (contentValues.containsKey("image")) {
                    image = contentValues.getAsString("image");
                } else {
                    return null;
                }

                Trip newTrip = new Trip(date,distance,movementType,timeInSeconds,routePoints,elevationData,caloriesBurned,weather,image);
                long tripID = database.tripDao().addNewTrip(newTrip);
                if (tripID > 0) {
                    returnUri = ContentUris.withAppendedId(uri, tripID);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;
            case SAVED_LOCATIONS:
                break;
            // Handle other cases (GOALS, TRIPS, etc.) similarly
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public Date parseDateString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust the pattern to match your date format
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("Provider", "Error parsing the date: " + e.getMessage());
            return null; // or handle the error as appropriate
        }
    }
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
