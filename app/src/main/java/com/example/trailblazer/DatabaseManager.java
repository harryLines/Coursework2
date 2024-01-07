package com.example.trailblazer;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

@Singleton
public class DatabaseManager {
    private static Database databaseInstance;

    public static synchronized Database getInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = Room.databaseBuilder(context, Database.class, "trailBlazerDatabase")
                    .build();
        }
        return databaseInstance;
    }
}