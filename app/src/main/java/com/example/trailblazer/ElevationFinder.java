package com.example.trailblazer;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ElevationFinder {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/elevation/json";
    private static final String API_KEY = "AIzaSyCrKsxTguyZRaVlFrC9ADqGZbmLKyxctWs"; // Replace with your actual API key

    public interface ElevationCallback {
        void onElevationReceived(double elevation);

        void onError(String errorMessage);
    }

    public static void getElevation(double latitude, double longitude, ElevationCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Double> future = executor.submit(new ElevationCallable(latitude, longitude));

        try {
            double elevation = future.get(); // This blocks until the result is available
            if (!Double.isNaN(elevation)) {
                callback.onElevationReceived(elevation);
            } else {
                callback.onError("Failed to retrieve elevation");
            }
        } catch (Exception e) {
            callback.onError("Failed to retrieve elevation: " + e.getMessage());
            Log.e("ElevationFinder", "Exception in getElevation", e); // Log the exception for debugging
        } finally {
            executor.shutdown(); // Don't forget to shutdown the executor
        }
    }


    private static class ElevationCallable implements Callable<Double> {
        private final double latitude;
        private final double longitude;

        ElevationCallable(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public Double call() {
            try {
                // Construct the API request URL
                String requestUrl = BASE_URL +
                        "?locations=" + latitude + "," + longitude +
                        "&key=" + API_KEY;

                // Create a URL object
                URL url = new URL(requestUrl);

                // Open a connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set up the connection
                connection.setRequestMethod("GET");
                connection.connect();

                // Check the HTTP response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read and parse the JSON response
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse the JSON response to extract elevation
                    return parseElevationFromJson(response.toString());
                } else {
                    // Handle the error
                    Log.e("ElevationApiHelper", "Error response code: " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Double.NaN; // Return NaN if an error occurs
        }
    }

    private static double parseElevationFromJson(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray results = jsonObject.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject result = results.getJSONObject(0);
                return result.getDouble("elevation");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Double.NaN; // Return NaN if parsing fails
    }
}
