package com.example.TrailBlazer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "trailBlazerDatabase.db";
    private static final int DATABASE_VERSION = 2;
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

    private static final String CREATE_TRIP_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_TRIP_HISTORY + " (" +
                    COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MOVEMENT_TYPE + " INTEGER, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_DISTANCE_TRAVELED + " REAL, " +
                    COLUMN_TIME + " INTEGER, " +
                    COLUMN_ROUTE_POINTS + " TEXT" + ");";

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
                new String[]{COLUMN_TRIP_ID, COLUMN_MOVEMENT_TYPE, COLUMN_DATE, COLUMN_DISTANCE_TRAVELED, COLUMN_TIME, COLUMN_ROUTE_POINTS},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long tripId = cursor.getLong(cursor.getColumnIndex(COLUMN_TRIP_ID));
                @SuppressLint("Range") int movementType = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVEMENT_TYPE));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                @SuppressLint("Range") double distanceTraveled = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE_TRAVELED));
                @SuppressLint("Range") String latLongPoints = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_POINTS));
                @SuppressLint("Range") long time = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME));

                Trip tripItem = new Trip(parseDateString(date), tripId, distanceTraveled, movementType, time, parseRoutePoints(latLongPoints));
                tripHistory.add(tripItem);

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
        List<LatLng> routePoints = new ArrayList<>();

        try {
            // Convert JSON array to List<LatLng>
            LatLng[] latLngArray = new Gson().fromJson(routePointsJson, LatLng[].class);
            if (latLngArray != null) {
                Collections.addAll(routePoints, latLngArray);
            }

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return routePoints;
    }


    public long insertTripHistory(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVEMENT_TYPE, trip.getMovementType());
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
                .format(trip.getDate()));
        values.put(COLUMN_DISTANCE_TRAVELED, trip.getDistance());
        values.put(COLUMN_ROUTE_POINTS, trip.getRoutePoints().toString());
        values.put(COLUMN_TIME,trip.getTimeInSeconds());

        long tripId = db.insert(TABLE_TRIP_HISTORY, null, values);

        //db.close();

        return tripId;
    }

    public long saveLocation(String name, LatLng latLng) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_NAME, name);
        values.put(COLUMN_LOCATION_LATITUDE, latLng.latitude);
        values.put(COLUMN_LOCATION_LONGITUDE, latLng.longitude);
        long locationId = db.insert(TABLE_SAVED_LOCATIONS, null, values);
        //db.close();
        return locationId;
    }


    public void saveReminder(long locationId, String reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_LOCATION_ID, locationId);
        values.put(COLUMN_REMINDER_TEXT, reminder);
        db.insert(TABLE_REMINDERS, null, values);
        //db.close();
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

        //db.close();

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

}
