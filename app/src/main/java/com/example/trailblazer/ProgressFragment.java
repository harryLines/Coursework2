package com.example.trailblazer;

import static com.example.trailblazer.ProgressCalculations.calculateAverageSpeed;
import static com.example.trailblazer.ProgressCalculations.calculatePercentageChange;
import static com.example.trailblazer.ProgressCalculations.calculateTotalDistance;
import static com.example.trailblazer.ProgressCalculations.calculateTotalTime;
import static com.example.trailblazer.ProgressCalculations.formatDistance;
import static com.example.trailblazer.ProgressCalculations.formatSpeed;
import static com.example.trailblazer.ProgressCalculations.formatTime;
import static com.example.trailblazer.ProgressCalculations.formatTimeChange;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible for displaying and updating user progress statistics
 * based on different timeframes (1 Week, 1 Month, 3 Months, 6 Months). It loads trip history data
 * from the database and calculates statistics for walking, running, and cycling.
 */
public class ProgressFragment extends Fragment {
    List<Trip> tripHistory;
    private String prevSelectedTimeframe = "1 Week";
    public Database database;
    public ProgressFragment() {
        tripHistory = new ArrayList<>();
    }

    /**
     * Called when the fragment's view is created. Initializes the UI components, sets up the Spinner
     * for selecting timeframes, and registers listeners to handle timeframe changes.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle containing the saved state of the fragment.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.progress_fragment, container, false);

        database = DatabaseManager.getInstance(requireContext());

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

        return view;
    }

    /**
     * Displays a Toast message with the specified message.
     *
     * @param message The message to be displayed in the Toast.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the fragment is resumed. Loads trip history data from the database and updates
     * the statistics based on the previously selected timeframe.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Update the statistics based on the previously selected timeframe
        loadTripHistory();
    }

    /**
     * Updates statistics based on the selected timeframe and displays the results on the UI.
     *
     * @param selectedTimeframe The selected timeframe for which statistics are to be calculated.
     */
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
            double totalSpeedCurrent = calculateAverageSpeed(tripHistoryCurrentTimeframe, movementType);
            long totalTimeCurrent = calculateTotalTime(tripHistoryCurrentTimeframe, movementType);

            double totalDistancePrevious = calculateTotalDistance(tripHistoryPreviousTimeframe, movementType);
            double totalSpeedPrevious = calculateAverageSpeed(tripHistoryPreviousTimeframe, movementType);
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

    /**
     * Updates the UI components with calculated statistics and percentage change.
     *
     * @param movementType        The movement type for which statistics are updated.
     * @param totalDistance       The total distance traveled for the specified movement type.
     * @param totalSpeed          The average speed for the specified movement type.
     * @param totalTime           The total time spent for the specified movement type.
     * @param percentChangeDistance Percentage change in total distance.
     * @param percentChangeSpeed    Percentage change in average speed.
     * @param percentChangeTime     Percentage change in total time.
     */
    private void updateUI(int movementType, double totalDistance, double totalSpeed, long totalTime,
                          int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {

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

    /**
     * Updates the text color of a TextView based on the provided percentage change value.
     *
     * @param percentChange The percentage change value to determine the text color.
     * @param textViewid    The resource ID of the TextView to update.
     */
    private void updateSingleTextViewColour(int percentChange, int textViewid) {
        TextView textView = requireView().findViewById(textViewid);
        int color;

        // Set the background color based on the percentage change
        if (percentChange > 0) {
            // Positive percentage change, set background to green
            color = ContextCompat.getColor(requireContext(), R.color.positiveChangeText);
        } else if (percentChange < 0) {
            // Negative percentage change, set background to red
            color = ContextCompat.getColor(requireContext(), R.color.negativeChangeText);
        } else {
            // No change, set a default background color
            color = ContextCompat.getColor(requireContext(), R.color.white);
        }

        textView.setTextColor(color);
    }

    /**
     * Updates the background color of a CardView based on the percentage change.
     *
     * @param percentChange   The percentage change to determine the background color.
     * @param cardViewId      The resource ID of the CardView to update.
     */
    private void updateSingleCardViewBackground(int percentChange, int cardViewId) {
        View cardView = requireView().findViewById(cardViewId);

        // Create a GradientDrawable
        GradientDrawable gradientDrawable = new GradientDrawable();

        // Set the shape (RECTANGLE is the default)
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        // Set the corner radius (adjust as needed)
        gradientDrawable.setCornerRadius(8);

        // Set the gradient colors based on the percentage change
        if (percentChange > 0) {
            // Positive percentage change, set gradient from green to a lighter green
            int startColor = ContextCompat.getColor(requireContext(), R.color.positiveChangeStart);
            int endColor = ContextCompat.getColor(requireContext(), R.color.positiveChangeEnd);
            gradientDrawable.setColors(new int[]{startColor, endColor});
        } else if (percentChange < 0) {
            // Negative percentage change, set gradient from red to a lighter red
            int startColor = ContextCompat.getColor(requireContext(), R.color.negativeChangeStart);
            int endColor = ContextCompat.getColor(requireContext(), R.color.negativeChangeEnd);
            gradientDrawable.setColors(new int[]{startColor, endColor});
        } else {
            // No change, set a default background color (black)
            gradientDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.black));
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

    /**
     * Loads trip history from the database and updates statistics for the previously selected timeframe.
     */
    private void loadTripHistory() {
        // Load trip history from the database
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            tripHistory = database.tripDao().loadTripHistory();
            //Background work here
            handler.post(() -> {
                updateStatistics(prevSelectedTimeframe);
            });
        });
    }

    /**
     * Updates statistics and UI components specific to the "Walking" movement type.
     *
     * @param totalDistance       The total distance traveled while walking.
     * @param totalSpeed          The average speed while walking.
     * @param totalTime           The total time spent walking.
     * @param percentChangeDistance Percentage change in total walking distance.
     * @param percentChangeSpeed    Percentage change in average walking speed.
     * @param percentChangeTime     Percentage change in total walking time.
     */
    private void updateWalkingStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewWalkingDistanceValue = requireView().findViewById(R.id.textViewWalkingDistanceValue);
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

    /**
     * Updates statistics and UI components specific to the "Running" movement type.
     *
     * @param totalDistance       The total distance traveled while running.
     * @param totalSpeed          The average speed while running.
     * @param totalTime           The total time spent running.
     * @param percentChangeDistance Percentage change in total running distance.
     * @param percentChangeSpeed    Percentage change in average running speed.
     * @param percentChangeTime     Percentage change in total running time.
     */
    private void updateRunningStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewRunningDistanceValue = requireView().findViewById(R.id.textViewRunningDistanceValue);
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

    /**
     * Updates statistics and UI components specific to the "Cycling" movement type.
     *
     * @param totalDistance       The total distance traveled while cycling.
     * @param totalSpeed          The average speed while cycling.
     * @param totalTime           The total time spent cycling.
     * @param percentChangeDistance Percentage change in total cycling distance.
     * @param percentChangeSpeed    Percentage change in average cycling speed.
     * @param percentChangeTime     Percentage change in total cycling time.
     */
    private void updateCyclingStats(double totalDistance, double totalSpeed, long totalTime,
                                    int percentChangeDistance, int percentChangeSpeed, int percentChangeTime) {
        TextView textViewCyclingDistanceValue = requireView().findViewById(R.id.textViewCyclingDistanceValue);
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
}
