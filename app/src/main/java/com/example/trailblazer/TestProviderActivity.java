package com.example.trailblazer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestProviderActivity extends Activity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple layout with a TextView
        textView = new TextView(this);
        setContentView(textView);

        // Query the provider for data
        Uri tripURI = Uri.parse("content://" + Contract.AUTHORITY + "/trip_history");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Cursor cursor = getContentResolver().query(tripURI, null, null, null, null);
            handler.post(() -> {
                if (cursor != null && cursor.moveToFirst()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    do {
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("distance_traveled"));
                        stringBuilder.append(name).append("\n");
                    } while (cursor.moveToNext());

                    textView.setText(stringBuilder.toString());
                    cursor.close();
                } else {
                    textView.setText("No data found.");
                }
            });
        });
    }
}
