package com.example.trailblazer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trailBlazerDatabase.db";
    private static final int DATABASE_VERSION = 8;
    // Saved locations table
    public static final String TABLE_SAVED_LOCATIONS = "saved_locations";
    public static final String COLUMN_LOCATION_ID = "_id";
    public static final String COLUMN_LOCATION_NAME = "name";
    public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    // Reminders table
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_REMINDER_ID = "_id";
    public static final String COLUMN_REMINDER_LOCATION_ID = "location_id";
    public static final String COLUMN_REMINDER_TEXT = "reminder_text";

    //Trip History Table
    public static final String TABLE_TRIP_HISTORY = "trip_history";
    public static final String COLUMN_TRIP_ID = "_id";
    public static final String COLUMN_MOVEMENT_TYPE = "movement_type";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DISTANCE_TRAVELED = "distance_traveled";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_ROUTE_POINTS = "route_points";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_ELEVATION_DATA = "elevation_data";
    public static final String COLUMN_CALORIES_BURNED = "calories_burned";

    private static final String CREATE_TRIP_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_TRIP_HISTORY + " (" +
                    COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MOVEMENT_TYPE + " INTEGER, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_DISTANCE_TRAVELED + " REAL, " +
                    COLUMN_TIME + " INTEGER, " +
                    COLUMN_ROUTE_POINTS + " TEXT, " +
                    COLUMN_IMAGE_PATH + " TEXT, " +
                    COLUMN_CALORIES_BURNED + " TEXT, " +
                    COLUMN_ELEVATION_DATA + " TEXT" + ");";

    private static final String CREATE_SAVED_LOCATIONS_TABLE =
            "CREATE TABLE " + TABLE_SAVED_LOCATIONS + " (" +
                    COLUMN_LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LOCATION_NAME + " TEXT, " +
                    COLUMN_LOCATION_LATITUDE + " REAL, " +
                    COLUMN_LOCATION_LONGITUDE + " REAL);";

    private static final String CREATE_REMINDERS_TABLE =
            "CREATE TABLE " + TABLE_REMINDERS + " (" +
                    COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_REMINDER_LOCATION_ID + " INTEGER, " +
                    COLUMN_REMINDER_TEXT + " TEXT);";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the saved locations table
        db.execSQL(CREATE_SAVED_LOCATIONS_TABLE);

        // Create the reminders table
        db.execSQL(CREATE_REMINDERS_TABLE);

        db.execSQL(CREATE_TRIP_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades for each table if needed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_HISTORY);
        onCreate(db);
    }

    public List<Trip> loadTripHistory() throws ParseException {
        List<Trip> tripHistory = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRIP_HISTORY,
                new String[]{COLUMN_TRIP_ID, COLUMN_MOVEMENT_TYPE, COLUMN_DATE, COLUMN_DISTANCE_TRAVELED, COLUMN_TIME, COLUMN_ROUTE_POINTS,COLUMN_ELEVATION_DATA,COLUMN_CALORIES_BURNED},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long tripId = cursor.getLong(cursor.getColumnIndex(COLUMN_TRIP_ID));
                @SuppressLint("Range") int movementType = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVEMENT_TYPE));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") double distanceTraveled = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE_TRAVELED));
                @SuppressLint("Range") String latLongPoints = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_POINTS));
                @SuppressLint("Range") long time = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));
                @SuppressLint("Range") String elevationData = cursor.getString(cursor.getColumnIndex(COLUMN_ELEVATION_DATA));
                @SuppressLint("Range") int calories = cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES_BURNED));

                Trip tripItem = new Trip(parseDateString(date), tripId, distanceTraveled, movementType, time, parseRoutePoints(latLongPoints), parseElevationData(elevationData),calories);
                tripHistory.add(tripItem);
                Log.d("LOADED ROUTE POINTS:", parseRoutePoints(latLongPoints).toString());

            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return tripHistory;
    }

    private Date parseDateString(String dateString) throws ParseException {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
         return dateFormat.parse(dateString);
    }

    private List<LatLng> parseRoutePoints(String routePointsJson) {
        return new Gson().fromJson(routePointsJson, new TypeToken<List<LatLng>>(){}.getType());
    }

    private List<Double> parseElevationData(String elevationDataJson) {
        return new Gson().fromJson(elevationDataJson, new TypeToken<List<Double>>(){}.getType());
    }


    public long insertTripHistory(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVEMENT_TYPE, trip.getMovementType());
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(trip.getDate()));
        values.put(COLUMN_DISTANCE_TRAVELED, trip.getDistance());
        values.put(COLUMN_ROUTE_POINTS, convertRoutePointsToJson(trip.getRoutePoints()));
        values.put(COLUMN_TIME, trip.getTimeInSeconds());
        values.put(COLUMN_ELEVATION_DATA, convertElevationDataToJson(trip.getElevationData()));
        values.put(COLUMN_CALORIES_BURNED,trip.getCaloriesBurned());

        long tripId = db.insert(TABLE_TRIP_HISTORY, null, values);

        db.close();

        return tripId;
    }

    private String convertElevationDataToJson(List<Double> elevationData) {
        // Convert elevation data to JSON string or any other suitable format
        return new Gson().toJson(elevationData);
    }

    private String convertRoutePointsToJson(List<LatLng> routePoints) {
        // Convert elevation data to JSON string or any other suitable format
        return new Gson().toJson(routePoints);
    }



    public long saveLocation(String name, LatLng latLng) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_NAME, name);
        values.put(COLUMN_LOCATION_LATITUDE, latLng.latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, latLng.longitude);
        long locationId = db.insert(TABLE_SAVED_LOCATIONS, null, values);
        db.close();
        return locationId;
    }


    public void saveReminder(long locationId, String reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_LOCATION_ID, locationId);
        values.put(COLUMN_REMINDER_TEXT, reminder);
        db.insert(TABLE_REMINDERS, null, values);
        db.close();
    }

    public List<SavedLocation> loadSavedLocations() {
        List<SavedLocation> savedLocations = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_SAVED_LOCATIONS,
                new String[]{COLUMN_LOCATION_ID, COLUMN_LOCATION_NAME, COLUMN_LOCATION_LATITUDE, COLUMN_LOCATION_LONGITUDE},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long locationId = cursor.getLong(cursor.getColumnIndex(COLUMN_LOCATION_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION_NAME));
                @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LOCATION_LATITUDE));
                @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LOCATION_LONGITUDE));

                // Load reminders for the current saved location
                List<String> reminders = loadRemindersForLocation(locationId);

                LatLng latLng = new LatLng(latitude, longitude);
                SavedLocation savedLocation = new SavedLocation(locationId,name, latLng, reminders);

                savedLocations.add(savedLocation);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return savedLocations;
    }

    private List<String> loadRemindersForLocation(long locationId) {
        List<String> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_REMINDERS,
                new String[]{COLUMN_REMINDER_TEXT},
                COLUMN_REMINDER_LOCATION_ID + " = ?",
                new String[]{String.valueOf(locationId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String reminder = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TEXT));
                reminders.add(reminder);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return reminders;
    }

    @SuppressLint("Range")
    public List<String> loadRemindersForLocationName(String locationName) {
        List<String> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Query the saved locations table to get the locationId based on the locationName
        Cursor locationCursor = db.query(TABLE_SAVED_LOCATIONS,
                new String[]{COLUMN_LOCATION_ID},
                COLUMN_LOCATION_NAME + " = ?",
                new String[]{locationName},
                null, null, null);

        long locationId = -1;

        if (locationCursor != null && locationCursor.moveToFirst()) {
            locationId = locationCursor.getLong(locationCursor.getColumnIndex(COLUMN_LOCATION_ID));
            locationCursor.close();
        }

        // Check if a valid locationId is found
        if (locationId != -1) {
            // Query the reminders table to get reminders based on the locationId
            Cursor cursor = db.query(TABLE_REMINDERS,
                    new String[]{COLUMN_REMINDER_TEXT},
                    COLUMN_REMINDER_LOCATION_ID + " = ?",
                    new String[]{String.valueOf(locationId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String reminder = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_TEXT));
                    reminders.add(reminder);
                } while (cursor.moveToNext());

                cursor.close();
            }
        }

        db.close();
        return reminders;
    }

    public void deleteTripHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRIP_HISTORY, null, null);
        db.close();
    }

}
