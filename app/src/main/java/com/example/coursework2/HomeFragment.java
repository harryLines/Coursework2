package com.example.coursework2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextClock;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class HomeFragment extends Fragment {
    CheckBox checkboxWalking;
    CheckBox checkboxRunning;
    CheckBox checkboxCycling;
    public HomeFragment() {
    }

    private List<Trip> loadTripHistory() {
        List<Trip> tripHistory = new ArrayList<>();
        Log.d("TRIP LOAD", "BEGIN LOAD");
        try {
            File file = new File(getContext().getFilesDir(), "trip_history.txt");

            if (!file.exists()) {
                // File doesn't exist, return an empty list
                Log.d("TRIP LOAD", "NO FILE");
                return tripHistory;
            }
            Log.d("TRIP LOAD", "FILE FOUND");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Parse each line to create a Trip object
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int movementType = Integer.parseInt(parts[0].trim());
                    Date date = parseDate(parts[1].trim());
                    double distance = Double.parseDouble(parts[2].trim());
                    long time = Long.parseLong(parts[3].trim());

                    Trip trip = new Trip(date, distance, movementType,time);
                    Log.d("TRIP LOAD", String.valueOf(trip.getDistance()));
                    tripHistory.add(trip);
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tripHistory;
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Return the current date in case of parsing error
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        checkboxWalking = view.findViewById(R.id.checkBoxWalking);
        checkboxRunning = view.findViewById(R.id.checkBoxRunning);
        checkboxCycling = view.findViewById(R.id.checkBoxCycling);

        // Load trip history data
        List<Trip> tripHistory = loadTripHistory();

        // Find the WeeklyGraphView in the layout
        WeeklyGraphView weeklyGraphView = view.findViewById(R.id.weeklyGraphView);

        // Set a listener to handle checkbox selections
        checkboxWalking.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(tripHistory, weeklyGraphView));
        checkboxRunning.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(tripHistory, weeklyGraphView));
        checkboxCycling.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(tripHistory, weeklyGraphView));

        // Initial update
        updateGraph(tripHistory, weeklyGraphView);

        return view;
    }



    private List<Date> getDateListLastWeek(List<Trip> trips) {
        List<Date> dateListLastWeek = new ArrayList<>();
        HashSet<String> uniqueDates = new HashSet<>(); // Use a set to track unique dates

        for (Trip trip : trips) {
            String dateString = getDayFromDate(trip.getDate());
            if (uniqueDates.add(dateString)) { // If the date is unique, add it to the list
                dateListLastWeek.add(trip.getDate());
            }
        }

        return dateListLastWeek;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load the latest trip history
        List<Trip> tripHistory = loadTripHistory();

        // Filter walking trips
        List<Trip> walkingTrips = filterWalkingTrips(tripHistory);

        // Filter walking trips within the last week
        List<Trip> walkingTripsLastWeek = filterTripsLastWeek(walkingTrips);

        // Summarize distance for walking trips by day
        Map<String, Double> walkingDistanceByDay = calculateDistanceByDay(walkingTripsLastWeek);

        // Find the WeeklyGraphView in the layout
        WeeklyGraphView weeklyGraphView = getView().findViewById(R.id.weeklyGraphView);

        // Set the data to the WeeklyGraphView
        List<Date> dateListLastWeek = getDateListLastWeek(walkingTripsLastWeek);

        // Set the data to the WeeklyGraphView
        weeklyGraphView.setDataPoints(convertMapToList(walkingDistanceByDay), dateListLastWeek);
    }

    private List<Trip> filterWalkingTrips(List<Trip> trips) {
        List<Trip> walkingTrips = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getMovementType() == Trip.MOVEMENT_WALK) {
                walkingTrips.add(trip);
            }
        }
        return walkingTrips;
    }

    private List<Trip> filterTripsLastWeek(List<Trip> trips) {
        List<Trip> tripsLastWeek = new ArrayList<>();
        long oneWeekAgoMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds

        for (Trip trip : trips) {
            if (trip.getDate().getTime() >= oneWeekAgoMillis) {
                tripsLastWeek.add(trip);
            }
        }
        return tripsLastWeek;
    }

    private Map<String, Double> calculateDistanceByDay(List<Trip> walkingTrips) {
        Map<String, Double> walkingDistanceByDay = new TreeMap<>(); // TreeMap for sorting by day

        for (Trip trip : walkingTrips) {
            String day = getDayFromDate(trip.getDate());
            double distance = trip.getDistance();

            // Update the total distance for the day
            walkingDistanceByDay.put(day, walkingDistanceByDay.getOrDefault(day, 0.0) + distance);
        }

        return walkingDistanceByDay;
    }

    private List<Float> convertMapToList(Map<String, Double> data) {
        List<Float> dataList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataList.add(entry.getValue().floatValue());
        }
        return dataList;
    }

    private String getDayFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        return sdf.format(date);
    }

    private void updateGraph(List<Trip> tripHistory, WeeklyGraphView weeklyGraphView) {
        List<Trip> selectedTrips = new ArrayList<>();

        // Find the selected checkboxes

        // Filter the trips based on checkbox selections
        for (Trip trip : tripHistory) {
            switch (trip.getMovementType()) {
                case Trip.MOVEMENT_WALK:
                    if (checkboxWalking.isChecked()) {
                        selectedTrips.add(trip);
                    }
                    break;
                case Trip.MOVEMENT_RUN:
                    if (checkboxRunning.isChecked()) {
                        selectedTrips.add(trip);
                    }
                    break;
                case Trip.MOVEMENT_CYCLE:
                    if (checkboxCycling.isChecked()) {
                        selectedTrips.add(trip);
                    }
                    break;
            }
        }

        // Filter walking trips within the last week
        List<Trip> selectedTripsLastWeek = filterTripsLastWeek(selectedTrips);

        // Summarize distance for selected trips by day
        Map<String, Double> distanceByDay = calculateDistanceByDay(selectedTripsLastWeek);

        // Set the data to the WeeklyGraphView
        List<Date> dateListLastWeek = getDateListLastWeek(selectedTripsLastWeek);
        weeklyGraphView.setDataPoints(convertMapToList(distanceByDay), dateListLastWeek);
    }
}
