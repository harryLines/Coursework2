package com.example.trailblazer;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * The Database class represents the Room database for the App.
 * It defines the entities and database version.
 */
@androidx.room.Database(entities = {SavedLocation.class, Reminder.class, Trip.class, Goal.class}, version = 27)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {

    /**
     * Gets the Data Access Object (DAO) for SavedLocation entities.
     *
     * @return The SavedLocationDao for interacting with SavedLocation data.
     */
    public abstract SavedLocationDao savedLocationDao();

    /**
     * Gets the Data Access Object (DAO) for Trip entities.
     *
     * @return The TripDao for interacting with Trip data.
     */
    public abstract TripDao tripDao();

    /**
     * Gets the Data Access Object (DAO) for Goal entities.
     *
     * @return The GoalDao for interacting with Goal data.
     */
    public abstract GoalDao goalDao();

    /**
     * Gets the Data Access Object (DAO) for Reminder entities.
     *
     * @return The ReminderDao for interacting with Reminder data.
     */
    public abstract ReminderDao reminderDao();
}
