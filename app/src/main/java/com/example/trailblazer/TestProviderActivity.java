package com.example.trailblazer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestProviderActivity extends Activity {
    private TextView textView;
    int metricType = 1;
    int numberOfTimeframes = 7;
    int timeframeType = 1;
    double progress = 0.0;
    double target = 100.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_provider_activity);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Uri tripURI = Uri.parse("content://" + Contract.AUTHORITY + "/goals");
            ContentValues contentValues = new ContentValues();
            contentValues.put("metric_type", metricType);
            contentValues.put("number_of_timeframes", numberOfTimeframes);
            contentValues.put("timeframe_type", timeframeType);
            contentValues.put("progress", progress);
            contentValues.put("target", target);
            contentValues.put("date_created", Converters.dateToTimestamp(new Date()));
            contentValues.put("is_complete", false);

            Uri insertedUri = getContentResolver().insert(tripURI, contentValues);
            Log.d("OUT", String.valueOf(insertedUri));
        });
    }
}
