package com.example.trailblazer.data;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public GoalRepository provideGoalRepository(GoalDao goalDao) {
        return new GoalRepository(goalDao);
    }

    @Provides
    @Singleton
    public ReminderRepository provideReminderRepository(ReminderDao reminderDao) {
        return new ReminderRepository(reminderDao);
    }

    @Provides
    @Singleton
    public SavedLocationRepository provideSavedLocationRepository(SavedLocationDao savedLocationDao) {
        return new SavedLocationRepository(savedLocationDao);
    }

    @Provides
    @Singleton
    public TripRepository provideTripRepository(TripDao tripDao) {
        return new TripRepository(tripDao);
    }
}
