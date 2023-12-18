package com.example.trailblazer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Provider extends ContentProvider {
    private static final int URI_SAVED_LOCATIONS = 1;
    private static final int URI_REMINDERS = 2;
    private static final int URI_TRIP_HISTORY = 3;
    DatabaseManager dbManager;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(Contract.AUTHORITY, "saved_locations", URI_SAVED_LOCATIONS);
        uriMatcher.addURI(Contract.AUTHORITY, "reminders", URI_REMINDERS);
        uriMatcher.addURI(Contract.AUTHORITY, "trip_history", URI_TRIP_HISTORY);
    }

    @Override
    public boolean onCreate() {
        dbManager = new DatabaseManager(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbManager.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case URI_SAVED_LOCATIONS:
                cursor = db.query(DatabaseManager.TABLE_SAVED_LOCATIONS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_REMINDERS:
                cursor = db.query(DatabaseManager.TABLE_REMINDERS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case URI_TRIP_HISTORY:
                Log.d("TRIP","IS A TRIP REQUEST");
                cursor = db.query(DatabaseManager.TABLE_TRIP_HISTORY, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Notify the content resolver about changes in the data
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbManager.getWritableDatabase();
        long id;

        switch (uriMatcher.match(uri)) {
            case URI_SAVED_LOCATIONS:
                id = db.insert(DatabaseManager.TABLE_SAVED_LOCATIONS, null, values);
                break;
            case URI_REMINDERS:
                id = db.insert(DatabaseManager.TABLE_REMINDERS, null, values);
                break;
            case URI_TRIP_HISTORY:
                id = db.insert(DatabaseManager.TABLE_TRIP_HISTORY, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Notify the content resolver about changes in the data
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_SAVED_LOCATIONS:
                return "vnd.android.cursor.dir/saved_locations";
            case URI_REMINDERS:
                return "vnd.android.cursor.dir/reminders";
            case URI_TRIP_HISTORY:
                return "vnd.android.cursor.dir/trip_history";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
