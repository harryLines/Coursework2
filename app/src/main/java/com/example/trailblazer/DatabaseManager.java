package com.example.trailblazer;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

/**
 * The DatabaseManager class provides access to the Room database instance for the TrailBlazer app.
 * It follows the Singleton pattern to ensure a single instance of the database throughout the application.
 */
@Singleton
public class DatabaseManager {
    private static Database databaseInstance;

    /**
     * Gets the instance of the Room database.
     *
     * @param context The application context used to create the database instance.
     * @return The Room database instance.
     */
    public static synchronized Database getInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = Room.databaseBuilder(context, Database.class, "trailBlazerDatabase")
                    .build();
        }
        return databaseInstance;
    }
}
