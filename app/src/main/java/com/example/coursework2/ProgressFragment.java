package com.example.coursework2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    List<Trip> tripHistory;
    List<Trip> tripHistoryCurrentTimeframe;
    List<Trip> tripHistoryPreviousTimeframe;
    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.progress_fragment, container, false);

        // Get the Spinner from the layout
        Spinner spinnerDuration = view.findViewById(R.id.dropDownTimeframe);

        // Define the options for the Spinner
        String[] durationOptions = {"1 Week", "1 Month", "3 Months", "6 Months"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                durationOptions
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerDuration.setAdapter(adapter);

        // Set a listener to detect when a timeframe is selected
        spinnerDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Update the statistics based on the selected timeframe
                String selectedTimeframe = durationOptions[position];
                updateStatistics(selectedTimeframe);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        // Load trip history data
        tripHistory = loadTripHistory();

        return view;
    }

    private void updateStatistics(String selectedTimeframe) {
        // Clear previous statistics
        clearStatistics();

        // Get the start date based on the selected timeframe
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(); // End date is the current date
        calendar.setTime(endDate);

        List<Trip> tripHistoryCurrentTimeframe = new ArrayList<>();
        List<Trip> tripHistoryPreviousTimeframe = new ArrayList<>();

        switch (selectedTimeframe) {
            case "1 Week":
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case "1 Month":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "3 Months":
                calendar.add(Calendar.MONTH, -3);
                break;
            case "6 Months":
                calendar.add(Calendar.MONTH, -6);
                break;
            default:
                // Handle the default case or set a default timeframe
                break;
        }

        Date startDate = calendar.getTime();

        // Calculate the start date for the previous timeframe (e.g., 7 days before startDate)
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDatePreviousTimeframe = calendar.getTime();

        // Populate tripHistoryCurrentTimeframe and tripHistoryPreviousTimeframe
        for (Trip trip : tripHistory) {
            if (trip.getDate().after(startDate) && trip.getDate().before(endDate)) {
                tripHistoryCurrentTimeframe.add(trip);
            } else if (trip.getDate().after(startDatePreviousTimeframe) && trip.getDate().before(startDate)) {
                tripHistoryPreviousTimeframe.add(trip);
            }
        }

        // Calculate statistics for each movement type
        for (int movementType = 0; movementType <= 2; movementType++) {
            double totalDistanceCurrent = calculateTotalDistance(tripHistoryCurrentTimeframe, movementType);
            double totalSpeedCurrent = calculateTotalSpeed(tripHistoryCurrentTimeframe, movementType);
            long totalTimeCurrent = calculateTotalTime(tripHistoryCurrentTimeframe, movementType);

            double totalDistancePrevious = calculateTotalDistance(tripHistoryPreviousTimeframe, movementType);
            double totalSpeedPrevious = calculateTotalSpeed(tripHistoryPreviousTimeframe, movementType);
            long totalTimePrevious = calculateTotalTime(tripHistoryPreviousTimeframe, movementType);

            // Calculate percentage change compared to the previous timeframe
            int percentChangeDistance = calculatePercentageChange(totalDistanceCurrent, totalDistancePrevious);
            int percentChangeSpeed = calculatePercentageChange(totalSpeedCurrent, totalSpeedPrevious);
            int percentChangeTime = calculatePercentageChange(totalTimeCurrent, totalTimePrevious);

            // Update UI with calculated statistics and percentage change
            updateUI(movementType, totalDistanceCurrent, totalSpeedCurrent, totalTimeCurrent,
                    percentChangeDistance, percentChangeSpeed, percentChangeTime);
        }
    }

    // Helper methods to calculate total distance, speed, and time
    private double calculateTotalDistance(List<Trip> trips, int movementType) {
        double totalDistance = 0;
        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                totalDistance += trip.getDistance();
            }
        }
        return totalDistance;
    }

    private double calculateTotalSpeed(List<Trip> trips, int movementType) {
        double totalSpeed = 0;
        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                totalSpeed += calculateSpeed(trip.getDistance(), trip.getTimeInSeconds());
            }
        }
        return totalSpeed;
    }

    private long calculateTotalTime(List<Trip> trips, int movementType) {
        long totalTime = 0;
        for (Trip trip : trips) {
            if (trip.getMovementType() == movementType) {
                totalTime += trip.getTimeInSeconds();
            }
        }
        return totalTime;
    }

    // Helper method to calculate percentage change
    private int calculatePercentageChange(double currentValue, double previousValue) {
        if (previousValue != 0) {
            double percentageChange = ((currentValue - previousValue) / previousValue) * 100;
            return (int) percentageChange;
        } else {
            return 0; // Handle division by zero or when there's no previous value
        }
    }

    // Helper method to check if a date is within the previous week
    private boolean isWithinPreviousWeek(Date date, Date startDate, Date endDate) {
        return date.after(startDate) && date.before(endDate);
    }
    private int calculatePercentageChange(int movementType, double currentValue, String statistic) {
        // Calculate percentage change compared to the previous timeframe
        double previousValue = getPreviousValue(movementType, statistic);
        if (previousValue != 0) {
            double percentageChange = ((currentValue - previousValue) / previousValue) * 100;
            return (int) percentageChange;
        } else {
            return 0; // Handle division by zero or when there's no previous value
        }
    }

    private double getPreviousValue(int movementType, String statistic) {
        // Get the start and end dates for the last two weeks
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(); // End date is the current date
        calendar.setTime(endDate);

        // Calculate the start date for the last two weeks
        calendar.add(Calendar.DAY_OF_YEAR, -14);
        Date startDate = calendar.getTime();

        // Traverse the tripHistory list to find the value two weeks ago
        for (Trip trip : tripHistory) {
            if (trip.getMovementType() == movementType && isWithinPreviousWeek(trip.getDate(), startDate, endDate)) {
                switch (statistic) {
                    case "distance":
                        return trip.getDistance();
                    case "speed":
                        return calculateSpeed(trip.getDistance(), trip.getTimeInSeconds());
                    case "time":
                        return trip.getTimeInSeconds();
                    // Add more cases if needed for other statistics
                }
            }
        }
        // Return a default value if no previous value is found
        return 0;
    }




    private void clearStatistics() {
        // Implement logic to clear or reset UI elements showing statistics
        // For example:
        // Clear TextViews showing distance, speed, time, etc.

    }

    private double calculateSpeed(double distance, long time) {
        // Calculate average speed (speed = distance / time)
        if (time > 0) {
            return distance / time;
        } else {
            return 0; // Handle division by zero or negative time
        }
    }

    private void updateUI(int movementType, double totalDistance, double totalSpeed, long totalTime,
                          int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {

        String movementTypeName = getMovementTypeName(movementType);

        Log.d("UPDATE UI", "Movement Type: " + movementTypeName);
        Log.d("UPDATE UI", "Total Distance: " + totalDistance);
        Log.d("UPDATE UI", "Average Speed: " + totalSpeed);
        Log.d("UPDATE UI", "Total Time: " + totalTime);
        Log.d("UPDATE UI", "Distance Change: " + percentChangeDistance + "%");
        Log.d("UPDATE UI", "Speed Change: " + percentChangeSpeed + "%");
        Log.d("UPDATE UI", "Time Change: " + percentChangeTime + "%");
        Log.d("UPDATE UI", "----------------------");

        // Update the UI based on the movement type
        switch (movementType) {
            case Trip.MOVEMENT_WALK:
                updateWalkingStats(totalDistance, totalSpeed, totalTime, percentChangeDistance, percentChangeSpeed, percentChangeTime);
                updateCardViewBackground(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.cardViewWalkingDistance,
                        R.id.cardViewWalkingSpeed,
                        R.id.cardViewWalkingTime
                );
                updateWalkingTextViewColour(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.textViewWalkingDistanceChange,
                        R.id.textViewWalkingSpeedChange,
                        R.id.textViewWalkingTimeChange
                );
                break;
            case Trip.MOVEMENT_RUN:
                updateRunningStats(totalDistance, totalSpeed, totalTime, percentChangeDistance, percentChangeSpeed, percentChangeTime);
                updateCardViewBackground(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.cardViewRunningDistance,
                        R.id.cardViewRunningSpeed,
                        R.id.cardViewRunningTime
                );
                updateRunningTextViewColour(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.textViewRunningDistanceChange,
                        R.id.textViewRunningSpeedChange,
                        R.id.textViewRunningTimeChange
                );
                break;
            case Trip.MOVEMENT_CYCLE:
                updateCyclingStats(totalDistance, totalSpeed, totalTime, percentChangeDistance, percentChangeSpeed, percentChangeTime);
                updateCardViewBackground(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.cardViewCyclingDistance,
                        R.id.cardViewCyclingSpeed,
                        R.id.cardViewCyclingTime
                );
                updateCyclingTextViewColour(
                        percentChangeDistance,
                        percentChangeSpeed,
                        percentChangeTime,
                        R.id.textViewCyclingDistanceChange,
                        R.id.textViewCyclingSpeedChange,
                        R.id.textViewCyclingTimeChange
                );
                break;
        }
    }

    private void updateCardViewBackground(
            int percentChangeDistance,
            int percentChangeSpeed,
            int percentChangeTime,
            int cardViewDistanceId,
            int cardViewSpeedId,
            int cardViewTimeId
    ) {
        updateSingleCardViewBackground(percentChangeDistance, cardViewDistanceId);
        updateSingleCardViewBackground(percentChangeSpeed, cardViewSpeedId);
        updateSingleCardViewBackground(percentChangeTime, cardViewTimeId);
    }

    private void updateWalkingTextViewColour(
            int percentChangeDistance,
            int percentChangeSpeed,
            int percentChangeTime,
            int textViewWalkingDistanceId,
            int textViewWalkingSpeedId,
            int textViewWalkingTimeId
    ) {
        updateSingleTextViewColour(percentChangeDistance, textViewWalkingDistanceId);
        updateSingleTextViewColour(percentChangeSpeed, textViewWalkingSpeedId);
        updateSingleTextViewColour(percentChangeTime, textViewWalkingTimeId);
    }

    private void updateRunningTextViewColour(
            int percentChangeDistance,
            int percentChangeSpeed,
            int percentChangeTime,
            int textViewRunningDistanceId,
            int textViewRunningSpeedId,
            int textViewRunningTimeId
    ) {
        updateSingleTextViewColour(percentChangeDistance, textViewRunningDistanceId);
        updateSingleTextViewColour(percentChangeSpeed, textViewRunningSpeedId);
        updateSingleTextViewColour(percentChangeTime, textViewRunningTimeId);
    }

    private void updateCyclingTextViewColour(
            int percentChangeDistance,
            int percentChangeSpeed,
            int percentChangeTime,
            int textViewCyclingDistanceId,
            int textViewCyclingSpeedId,
            int textViewCyclingTimeId
    ) {
        updateSingleTextViewColour(percentChangeDistance, textViewCyclingDistanceId);
        updateSingleTextViewColour(percentChangeSpeed, textViewCyclingSpeedId);
        updateSingleTextViewColour(percentChangeTime, textViewCyclingTimeId);
    }

    private void updateSingleTextViewColour(int percentChange, int textViewid) {
        TextView textView = getView().findViewById(textViewid);
        int color;

        // Set the background color based on the percentage change
        if (percentChange > 0) {
            // Positive percentage change, set background to green
            color = getResources().getColor(R.color.positiveChangeText);
        } else if (percentChange < 0) {
            // Negative percentage change, set background to red
            color = getResources().getColor(R.color.negativeChangeText);
        } else {
            // No change, set a default background color
            color = getResources().getColor(R.color.white);
        }

        textView.setTextColor(color);
    }

    private void updateSingleCardViewBackground(int percentChange, int cardViewId) {
        View cardView = getView().findViewById(cardViewId);
        int color;

        // Set the background color based on the percentage change
        if (percentChange > 0) {
            // Positive percentage change, set background to green
            color = getResources().getColor(R.color.positiveChange);
        } else if (percentChange < 0) {
            // Negative percentage change, set background to red
            color = getResources().getColor(R.color.negativeChange);
        } else {
            // No change, set a default background color
            color = getResources().getColor(R.color.black);
        }

        cardView.setBackgroundColor(color);
    }


    private String getMovementTypeName(int movementType) {
        // Map movement type to a human-readable name
        switch (movementType) {
            case Trip.MOVEMENT_WALK:
                return "Walking";
            case Trip.MOVEMENT_RUN:
                return "Running";
            case Trip.MOVEMENT_CYCLE:
                return "Cycling";
            default:
                return "Unknown";
        }
    }

    private List<Trip> loadTripHistory() {
        tripHistory = new ArrayList<>();
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

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Return the current date in case of parsing error
        }
    }

    private void updateWalkingStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewWalkingDistanceValue = getView().findViewById(R.id.textViewWalkingDistanceValue);
        textViewWalkingDistanceValue.setText(formatDistance(totalDistance));

        TextView textViewWalkingDistanceChange = getView().findViewById(R.id.textViewWalkingDistanceChange);
        textViewWalkingDistanceChange.setText(percentChangeDistance + "%");

        TextView textViewWalkingSpeedValue = getView().findViewById(R.id.textViewWalkingSpeedValue);
        textViewWalkingSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewWalkingSpeedChange = getView().findViewById(R.id.textViewWalkingSpeedChange);
        textViewWalkingSpeedChange.setText(percentChangeSpeed + "%");

        TextView textViewWalkingTimeValue = getView().findViewById(R.id.textViewWalkingTimeValue);
        textViewWalkingTimeValue.setText(formatTime(totalTime));

        TextView textViewWalkingTimeChange = getView().findViewById(R.id.textViewWalkingTimeChange);
        textViewWalkingTimeChange.setText(formatTimeChange(percentChangeTime));
    }
    private void updateRunningStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewRunningDistanceValue = getView().findViewById(R.id.textViewRunningDistanceValue);
        textViewRunningDistanceValue.setText(formatDistance(totalDistance));

        TextView textViewRunningDistanceChange = getView().findViewById(R.id.textViewRunningDistanceChange);
        textViewRunningDistanceChange.setText(percentChangeDistance + "%");

        TextView textViewRunningSpeedValue = getView().findViewById(R.id.textViewRunningSpeedValue);
        textViewRunningSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewRunningSpeedChange = getView().findViewById(R.id.textViewRunningSpeedChange);
        textViewRunningSpeedChange.setText(percentChangeSpeed + "%");

        TextView textViewRunningTimeValue = getView().findViewById(R.id.textViewRunningTimeValue);
        textViewRunningTimeValue.setText(formatTime(totalTime));

        TextView textViewRunningTimeChange = getView().findViewById(R.id.textViewRunningTimeChange);
        textViewRunningTimeChange.setText(formatTimeChange(percentChangeTime));
    }

    private void updateCyclingStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewCyclingDistanceValue = getView().findViewById(R.id.textViewCyclingDistanceValue);
        textViewCyclingDistanceValue.setText(formatDistance(totalDistance));

        TextView textViewCyclingDistanceChange = getView().findViewById(R.id.textViewCyclingDistanceChange);
        textViewCyclingDistanceChange.setText(percentChangeDistance + "%");

        TextView textViewCyclingSpeedValue = getView().findViewById(R.id.textViewCyclingSpeedValue);
        textViewCyclingSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewCyclingSpeedChange = getView().findViewById(R.id.textViewCyclingSpeedChange);
        textViewCyclingSpeedChange.setText(percentChangeSpeed + "%");

        TextView textViewCyclingTimeValue = getView().findViewById(R.id.textViewCyclingTimeValue);
        textViewCyclingTimeValue.setText(formatTime(totalTime));

        TextView textViewCyclingTimeChange = getView().findViewById(R.id.textViewCyclingTimeChange);
        textViewCyclingTimeChange.setText(formatTimeChange(percentChangeTime));
    }

    private String formatDistance(double distance) {
        // Format distance with 2 decimal points and append " km"
        return String.format(Locale.getDefault(), "%.2f km", distance/1000);
    }

    private String formatSpeed(double speed) {
        // Format speed to one decimal place and append " m/s"
        return String.format(Locale.getDefault(), "%.1f m/s", speed);
    }

    private String formatTime(long timeInSeconds) {
        // Format time in hours, minutes, and seconds
        long hours = timeInSeconds / 3600;
        long minutes = (timeInSeconds % 3600) / 60;
        long seconds = timeInSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %02ds", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    private String formatTimeChange(int percentChangeTime) {
        // Format time change percentage with a sign
        return String.format(Locale.getDefault(), "%+d%%", percentChangeTime);
    }
}
