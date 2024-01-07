package com.example.trailblazer;

import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
@androidx.room.Database(entities = {SavedLocation.class, Reminder.class, Trip.class, Goal.class}, version = 27)
@TypeConverters({Converters.class})
public abstract class Database extends RoomDatabase {
    public abstract SavedLocationDao savedLocationDao();
    public abstract TripDao tripDao();
    public abstract GoalDao goalDao();
    public abstract ReminderDao reminderDao();
}
