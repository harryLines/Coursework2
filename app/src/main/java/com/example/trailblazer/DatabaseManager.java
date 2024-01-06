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
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseManager extends SQLiteOpenHelper {
    private static DatabaseManager instance;
    private static final String DATABASE_NAME = "trailBlazerDatabase.db";
    private static final int DATABASE_VERSION = 18;
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
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_ELEVATION_DATA = "elevation_data";
    public static final String COLUMN_CALORIES_BURNED = "calories_burned";
    public static final String COLUMN_WEATHER = "weather";

    //Goals table
    public static final String TABLE_GOALS = "goals";
    public static final String COLUMN_GOAL_ID = "_id";
    public static final String COLUMN_METRIC_TYPE = "metric_type";
    public static final String COLUMN_NUMBER_OF_TIMEFRAMES = "number_of_timeframes";
    public static final String COLUMN_TIMEFRAME_TYPE = "timeframe_type";
    public static final String COLUMN_PROGRESS = "progress";
    public static final String COLUMN_TARGET = "target";
    public static final String COLUMN_DATE_CREATED = "date_created";
    public static final String COLUMN_IS_COMPLETE = "is_complete";

    private static final String CREATE_TRIP_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_TRIP_HISTORY + " (" +
                    COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MOVEMENT_TYPE + " INTEGER, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_DISTANCE_TRAVELED + " REAL, " +
                    COLUMN_TIME + " INTEGER, " +
                    COLUMN_ROUTE_POINTS + " TEXT, " +
                    COLUMN_IMAGE + " TEXT, " +
                    COLUMN_CALORIES_BURNED + " TEXT, " +
                    COLUMN_WEATHER + " INTEGER, " +
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

    private static final String CREATE_GOALS_TABLE =
            "CREATE TABLE " + TABLE_GOALS + " (" +
                    COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NUMBER_OF_TIMEFRAMES + " INTEGER, " +
                    COLUMN_TIMEFRAME_TYPE + " INT," +
                    COLUMN_PROGRESS + " INTEGER, " +
                    COLUMN_TARGET + " INTEGER, " +
                    COLUMN_DATE_CREATED + " TEXT, " +
                    COLUMN_IS_COMPLETE + " INT," +
                    COLUMN_METRIC_TYPE + " INT);";

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SAVED_LOCATIONS_TABLE);

        db.execSQL(CREATE_GOALS_TABLE);

        db.execSQL(CREATE_REMINDERS_TABLE);

        db.execSQL(CREATE_TRIP_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades for each table if needed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
    }

    public List<Goal> loadGoals() {
        List<Goal> goalsList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_GOALS,
                new String[]{COLUMN_GOAL_ID, COLUMN_NUMBER_OF_TIMEFRAMES, COLUMN_TIMEFRAME_TYPE, COLUMN_PROGRESS, COLUMN_TARGET, COLUMN_METRIC_TYPE, COLUMN_DATE_CREATED,COLUMN_IS_COMPLETE},
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long goalId = cursor.getLong(cursor.getColumnIndex(COLUMN_GOAL_ID));
                @SuppressLint("Range") int numberOfTimeframes = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER_OF_TIMEFRAMES));
                @SuppressLint("Range") int timeframeType = cursor.getInt(cursor.getColumnIndex(COLUMN_TIMEFRAME_TYPE));
                @SuppressLint("Range") int progress = cursor.getInt(cursor.getColumnIndex(COLUMN_PROGRESS));
                @SuppressLint("Range") int target = cursor.getInt(cursor.getColumnIndex(COLUMN_TARGET));
                @SuppressLint("Range") int metricType = cursor.getInt(cursor.getColumnIndex(COLUMN_METRIC_TYPE));
                @SuppressLint("Range") String dateCreated = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_CREATED));
                @SuppressLint("Range") int isComplete = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETE));

                Goal goalItem = new Goal(goalId, metricType, numberOfTimeframes, timeframeType, progress, target, parseDateFromString(dateCreated));
                goalItem.isComplete = isComplete == 1;
                goalsList.add(goalItem);

            } while (cursor.moveToNext());

            cursor.close();
            for (Goal goal : goalsList) {
                Log.d("Goal", String.valueOf(goal.getMetricType()));
            }
        }

        db.close();

        return goalsList;
    }

    private Date parseDateFromString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle the exception as needed
        }
    }
    public void updateGoals(List<Goal> updatedGoals) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            for (Goal updatedGoal : updatedGoals) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NUMBER_OF_TIMEFRAMES, updatedGoal.getNumberOfTimeframes());
                values.put(COLUMN_TIMEFRAME_TYPE, updatedGoal.getTimeframeType());
                values.put(COLUMN_PROGRESS, updatedGoal.getProgress());
                values.put(COLUMN_TARGET, updatedGoal.getTarget());
                values.put(COLUMN_METRIC_TYPE, updatedGoal.getMetricType());
                values.put(COLUMN_DATE_CREATED, updatedGoal.getDateCreated().toString());
                values.put(COLUMN_IS_COMPLETE, updatedGoal.isComplete);

                // Update the goal in the database
                db.update(
                        TABLE_GOALS,
                        values,
                        COLUMN_GOAL_ID + " = ?",
                        new String[]{String.valueOf(updatedGoal.getGoalID())}
                );
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Trip> loadTripHistory() throws ParseException {
        List<Trip> tripHistory = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRIP_HISTORY,
                new String[]{COLUMN_TRIP_ID, COLUMN_MOVEMENT_TYPE, COLUMN_DATE, COLUMN_DISTANCE_TRAVELED, COLUMN_TIME, COLUMN_ROUTE_POINTS,COLUMN_ELEVATION_DATA,COLUMN_CALORIES_BURNED,COLUMN_WEATHER,COLUMN_IMAGE},
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
                @SuppressLint("Range") int weather = cursor.getInt(cursor.getColumnIndex(COLUMN_WEATHER));
                @SuppressLint("Range") String image = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE));

                Trip tripItem = new Trip(parseDateString(date), tripId, distanceTraveled, movementType, time, parseRoutePoints(latLongPoints), parseElevationData(elevationData),calories,weather,image);
                tripHistory.add(tripItem);
                Log.d("LOADED ROUTE POINTS:", parseRoutePoints(latLongPoints).toString());

            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        return tripHistory;
    }

    private byte[] convertImagePathToByteArray(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null;
        }
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


    public void insertTripHistory(Trip trip) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MOVEMENT_TYPE, trip.getMovementType());
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(trip.getDate()));
        values.put(COLUMN_DISTANCE_TRAVELED, trip.getDistance());
        values.put(COLUMN_ROUTE_POINTS, convertRoutePointsToJson(trip.getRoutePoints()));
        values.put(COLUMN_TIME, trip.getTimeInSeconds());
        values.put(COLUMN_ELEVATION_DATA, convertElevationDataToJson(trip.getElevationData()));
        values.put(COLUMN_CALORIES_BURNED, trip.getCaloriesBurned());
        values.put(COLUMN_WEATHER,trip.getWeather());
        values.put(COLUMN_IMAGE,trip.getImage());

        long tripId = db.insert(TABLE_TRIP_HISTORY, null, values);

        db.close();

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

        // Check if the reminder already exists for the given location
        if (!reminderExists(locationId, reminder)) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_REMINDER_LOCATION_ID, locationId);
            values.put(COLUMN_REMINDER_TEXT, reminder);
            db.insert(TABLE_REMINDERS, null, values);
        }

        db.close();
    }

    private boolean reminderExists(long locationId, String reminder) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_REMINDERS,
                new String[]{COLUMN_REMINDER_TEXT},
                COLUMN_REMINDER_LOCATION_ID + " = ? AND " + COLUMN_REMINDER_TEXT + " = ?",
                new String[]{String.valueOf(locationId), reminder},
                null, null, null);

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    public void deleteReminders() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REMINDERS, null, null);
        db.close();
    }

    public void deleteSavedLocations() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SAVED_LOCATIONS, null, null);
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

    public void addNewGoal(Goal goal) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NUMBER_OF_TIMEFRAMES, goal.getNumberOfTimeframes());
        values.put(COLUMN_TIMEFRAME_TYPE, goal.getTimeframeType());
        values.put(COLUMN_PROGRESS, goal.getProgress());
        values.put(COLUMN_TARGET, goal.getTarget());
        values.put(COLUMN_METRIC_TYPE, goal.getMetricType());
        values.put(COLUMN_DATE_CREATED,goal.getDateCreated().toString());
        if(goal.isComplete){
            values.put(COLUMN_IS_COMPLETE, 1);
        } else {
            values.put(COLUMN_IS_COMPLETE, 0);
        }
        db.insert(TABLE_GOALS, null, values);
        db.close();
    }

    public void deleteTripHistory() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRIP_HISTORY, null, null);
        db.close();
    }

    public void removeReminder(String reminder, long locationID) {
        SQLiteDatabase db = getWritableDatabase();

        // Delete the reminder entry from the TABLE_REMINDERS
        db.delete(TABLE_REMINDERS,
                COLUMN_REMINDER_TEXT + " = ? AND " + COLUMN_REMINDER_LOCATION_ID + " = ?",
                new String[]{reminder, String.valueOf(locationID)});

        Log.d("REMINDEREMOVE","REMOVED" + reminder + "WITH ID" + locationID);


        db.close();
    }

}
