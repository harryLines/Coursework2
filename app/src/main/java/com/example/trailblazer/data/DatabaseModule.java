package com.example.trailblazer.data;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public Database provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, Database.class, "trailBlazerDatabase").build();
    }

    @Provides
    public GoalDao provideGoalDao(Database database) {
        return database.goalDao();
    }
    @Provides
    public ReminderDao provideReminderDao(Database database) {
        return database.reminderDao();
    }
    @Provides
    public TripDao provideTripDao(Database database) {
        return database.tripDao();
    }
    @Provides
    public SavedLocationDao provideSavedLocationDao(Database database) {
        return database.savedLocationDao();
    }
}
