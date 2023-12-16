package com.example.TrailBlazer;

import android.net.Uri;

public final class Contract {
    public static final String AUTHORITY = "com.example.TrailBlazer.provider";
    // Base content URI for accessing the provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    // Saved locations table
    public static final String TABLE_SAVED_LOCATIONS = "saved_locations";
    public static final Uri CONTENT_URI_SAVED_LOCATIONS = BASE_CONTENT_URI.buildUpon().appendPath("saved_locations").build();

    // Reminders table
    public static final String TABLE_REMINDERS = "reminders";
    public static final Uri CONTENT_URI_REMINDERS = BASE_CONTENT_URI.buildUpon().appendPath("reminders").build();

    // Trip history table
    public static final String TABLE_TRIP_HISTORY = "trip_history";
    public static final Uri CONTENT_URI_TRIP_HISTORY = BASE_CONTENT_URI.buildUpon().appendPath("trip_history").build();
}
