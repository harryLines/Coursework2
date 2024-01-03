package com.example.trailblazer;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    List<Trip> tripHistory;
    private String prevSelectedTimeframe = "1 Week";
    private DatabaseManager dbManager;
    public ProgressFragment(DatabaseManager dbManager) {
        this.dbManager = dbManager;
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
                if (!selectedTimeframe.equals(prevSelectedTimeframe)) {// Update the previous selected timeframe
                    showToast("You are now viewing progress in the last: " + selectedTimeframe);
                }
                updateStatistics(selectedTimeframe);
                prevSelectedTimeframe = selectedTimeframe;
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the statistics based on the previously selected timeframe
        updateStatistics(prevSelectedTimeframe);
        tripHistory = loadTripHistory();
    }


    private void updateStatistics(String selectedTimeframe) {
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
            double percentageChange = ((currentValue - previousValue) / Math.abs(previousValue)) * 100;
            return (int) percentageChange;
        } else {
            return 0; // Handle division by zero or when there's no previous value
        }
    }


    private double calculateSpeed(double distance, long time) {
        // Calculate average speed in kilometers per hour
        if (time > 0) {
            double speedInMetersPerSecond = distance / time;
            return speedInMetersPerSecond * 3.6; // Convert to kilometers per hour
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

        // Create a GradientDrawable
        GradientDrawable gradientDrawable = new GradientDrawable();

        // Set the shape (RECTANGLE is the default)
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        // Set the corner radius (adjust as needed)
        gradientDrawable.setCornerRadius(8);

        // Set the gradient colors based on the percentage change
        if (percentChange > 0) {
            // Positive percentage change, set gradient from green to a lighter green
            int startColor = getResources().getColor(R.color.positiveChangeStart);
            int endColor = getResources().getColor(R.color.positiveChangeEnd);
            gradientDrawable.setColors(new int[]{startColor, endColor});
        } else if (percentChange < 0) {
            // Negative percentage change, set gradient from red to a lighter red
            int startColor = getResources().getColor(R.color.negativeChangeStart);
            int endColor = getResources().getColor(R.color.negativeChangeEnd);
            gradientDrawable.setColors(new int[]{startColor, endColor});
        } else {
            // No change, set a default background color (black)
            gradientDrawable.setColor(getResources().getColor(R.color.black));
        }

        // Set the GradientDrawable as the background of the CardView
        cardView.setBackground(gradientDrawable);
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
        List<Trip> tripHistory = new ArrayList<>();
        Log.d("TRIP LOAD", "BEGIN LOAD");

        try {
            // Initialize your DatabaseManager
            DatabaseManager databaseManager = new DatabaseManager(requireContext());

            // Load trip history from the database
            tripHistory = databaseManager.loadTripHistory();
            Log.d("TRIP LOAD", "Number of trips loaded from the database: " + tripHistory.size());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tripHistory;
    }

    private void updateWalkingStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewWalkingDistanceValue = getView().findViewById(R.id.textViewWalkingDistanceValue);
        textViewWalkingDistanceValue.setText(formatDistance(totalDistance));

        TextView textViewWalkingDistanceChange = getView().findViewById(R.id.textViewWalkingDistanceChange);
        textViewWalkingDistanceChange.setText(formatTimeChange(percentChangeDistance));

        TextView textViewWalkingSpeedValue = getView().findViewById(R.id.textViewWalkingSpeedValue);
        textViewWalkingSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewWalkingSpeedChange = getView().findViewById(R.id.textViewWalkingSpeedChange);
        textViewWalkingSpeedChange.setText(formatTimeChange(percentChangeSpeed));

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
        textViewRunningDistanceChange.setText(formatTimeChange(percentChangeDistance));

        TextView textViewRunningSpeedValue = getView().findViewById(R.id.textViewRunningSpeedValue);
        textViewRunningSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewRunningSpeedChange = getView().findViewById(R.id.textViewRunningSpeedChange);
        textViewRunningSpeedChange.setText(formatTimeChange(percentChangeSpeed));

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
        textViewCyclingDistanceChange.setText(formatTimeChange(percentChangeDistance));

        TextView textViewCyclingSpeedValue = getView().findViewById(R.id.textViewCyclingSpeedValue);
        textViewCyclingSpeedValue.setText(formatSpeed(totalSpeed));

        TextView textViewCyclingSpeedChange = getView().findViewById(R.id.textViewCyclingSpeedChange);
        textViewCyclingSpeedChange.setText(formatTimeChange(percentChangeSpeed));

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
        return String.format(Locale.getDefault(), "%.1f km/h", speed);
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
        return String.format(Locale.getDefault(), "%s%d%%", (percentChangeTime >= 0) ? "+" : "", percentChangeTime);
    }
}
