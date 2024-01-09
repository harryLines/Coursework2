package com.example.trailblazer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.adapters.Converters;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Provider extends ContentProvider {
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int REMINDERS = 100;
    private static final int REMINDER_WITH_ID = 101;
    private static final int TRIPS = 102;
    private static final int TRIPS_WITH_ID = 103;
    private static final int GOALS = 104;
    private static final int GOALS_WITH_ID = 105;
    private static final int SAVED_LOCATIONS = 106;
    private static final int SAVED_LOCATIONS_WITH_ID = 107;
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
        return null;
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
