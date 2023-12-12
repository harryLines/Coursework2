package com.example.coursework2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

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
    TextView txtViewAvgWalkSpeed;
    TextView txtViewAvgRunSpeed;
    TextView txtViewAvgCycleSpeed;
    WeeklyGraphView weeklyGraphView;
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
                // Parse each line to create the Trip object
                String[] parts = line.split(",");
                    int movementType = Integer.parseInt(parts[0].trim());
                    Date date = parseDate(parts[1].trim());
                    double distance = Double.parseDouble(parts[2].trim());
                    long time = Long.parseLong(parts[3].trim());

                    Trip trip = new Trip(date, distance, movementType,time,null);
                    Log.d("TRIP LOAD", String.valueOf(trip.getDistance()));
                    tripHistory.add(trip);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tripHistory;
    }

    private double calculateAverageSpeed(List<Trip> trips, int movementType) {
        double totalSpeed = 0.0;
        int count = 0;

        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                double distance = trip.getDistance();
                long time = trip.getTimeInSeconds();

                // Calculate speed (speed = distance / time)
                double speed = (time > 0) ? (distance / time) : 0.0;

                totalSpeed += speed;
                count++;
            }
        }

        return (count > 0) ? (totalSpeed / count) : 0.0;
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
        txtViewAvgWalkSpeed = view.findViewById(R.id.textViewWalkingSpeed);
        txtViewAvgRunSpeed = view.findViewById(R.id.textViewRunningSpeed);
        txtViewAvgCycleSpeed = view.findViewById(R.id.textViewCyclingSpeed);

        // Load trip history data
        List<Trip> tripHistory = loadTripHistory();

        // Find the WeeklyGraphView in the layout
        weeklyGraphView = view.findViewById(R.id.weeklyGraphView);

        // Set a listener to handle checkbox selections
        checkboxWalking.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(weeklyGraphView));
        checkboxRunning.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(weeklyGraphView));
        checkboxCycling.setOnCheckedChangeListener((buttonView, isChecked) -> updateGraph(weeklyGraphView));

        // Initial update
        updateGraph(weeklyGraphView);

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
        updateGraph(weeklyGraphView);
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

    private void updateGraph(WeeklyGraphView weeklyGraphView) {
        List<Trip> selectedTrips = new ArrayList<>();
        List<Trip> tripHistory = loadTripHistory();
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

        // Filter trips within the last week
        List<Trip> selectedTripsLastWeek = filterTripsLastWeek(selectedTrips);

        // Summarize distance and time for selected trips by day
        Map<String, Double> distanceByDay = calculateDistanceByDay(selectedTripsLastWeek);

        // Set the data to the WeeklyGraphView
        List<Date> dateListLastWeek = getDateListLastWeek(selectedTripsLastWeek);
        weeklyGraphView.setDataPoints(
                convertMapToList(distanceByDay),
                dateListLastWeek
        );

        // Calculate and set the average speed for each movement type
        double avgWalkSpeed = calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_WALK);
        double avgRunSpeed = calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_RUN);
        double avgCycleSpeed = calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_CYCLE);

        // Set the average speed values to TextViews
        txtViewAvgWalkSpeed.setText((avgWalkSpeed > 0)
                ? String.format(Locale.UK, "Avg Walk Speed: %.2f m/s", avgWalkSpeed)
                : "");

        txtViewAvgRunSpeed.setText((avgRunSpeed > 0)
                ? String.format(Locale.UK, "Avg Run Speed: %.2f m/s", avgRunSpeed)
                : "");

        txtViewAvgCycleSpeed.setText((avgCycleSpeed > 0)
                ? String.format(Locale.UK, "Avg Cycle Speed: %.2f m/s", avgCycleSpeed)
                : "");
    }


}
