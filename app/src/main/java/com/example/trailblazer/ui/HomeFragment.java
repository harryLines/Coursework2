package com.example.trailblazer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.trailblazer.R;
import com.example.trailblazer.data.DatabaseManager;
import com.example.trailblazer.data.Trip;
import com.example.trailblazer.data.TripRepository;
import com.example.trailblazer.databinding.HomeFragmentBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The HomeFragment class is responsible for displaying and managing user's home screen.
 * It provides features like tracking walking, running, and cycling activities, displaying
 * average speeds, and showing weekly graphs for distance and calories.
 */
public class HomeFragment extends Fragment implements DefaultLifecycleObserver{
    CheckBox checkboxWalking;
    CheckBox checkboxRunning;
    CheckBox checkboxCycling;
    CheckBox checkBoxDistance;
    CheckBox checkboxCalories;
    TextView txtViewAvgWalkSpeed;
    TextView txtViewAvgRunSpeed;
    TextView txtViewAvgCycleSpeed;
    WeeklyGraphView weeklyGraphView;
    HomeFragmentViewModel viewModel;
    TripRepository tripRepository;
    HomeFragmentBinding binding;
    private boolean prevWalkingChecked = true;
    private boolean prevRunningChecked = false;
    private boolean prevCyclingChecked = false;
    public HomeFragment() {
    }

    /**
     * Calculates the average speed for a given list of trips of a specific movement type.
     *
     * @param trips        The list of trips to calculate the average speed from.
     * @param movementType The movement type for which to calculate the average speed.
     * @return The calculated average speed.
     */
    public double calculateAverageSpeed(List<Trip> trips, int movementType) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);

        // Create an instance of your ViewModel
        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
        tripRepository = new TripRepository(DatabaseManager.getInstance(requireContext()).tripDao());
        // Set the ViewModel to the binding
        binding.setViewModel(viewModel);

        // Set the lifecycle owner for LiveData observation
        binding.setLifecycleOwner(this);

        checkboxWalking = binding.checkBoxWalking;
        checkboxRunning = binding.checkBoxRunning;
        checkboxCycling = binding.checkBoxCycling;
        checkBoxDistance = binding.checkBoxDistance;
        checkboxCalories = binding.checkBoxCalories;
        txtViewAvgWalkSpeed = binding.textViewWalkingSpeed;
        txtViewAvgRunSpeed = binding.textViewRunningSpeed;
        txtViewAvgCycleSpeed = binding.textViewCyclingSpeed;
        weeklyGraphView = binding.weeklyGraphViewDistance;

        viewModel.setWalkingChecked(true);
        viewModel.setRunningChecked(false);
        viewModel.setCyclingChecked(false);

        // Load trip history data
        viewModel.isWalkingChecked().observe(getViewLifecycleOwner(), isChecked -> {
            try {
                if (isChecked != prevWalkingChecked) {
                    updateGraph();
                    prevWalkingChecked = isChecked;
                    if (isChecked) {
                        showToast("Walking is now enabled.");
                    } else {
                        showToast("Walking is now disabled.");
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        viewModel.isRunningChecked().observe(getViewLifecycleOwner(), isChecked -> {
            try {
                if (isChecked != prevRunningChecked) {
                    updateGraph();
                    prevRunningChecked = isChecked;
                    if (isChecked) {
                        showToast("Running is now enabled.");
                    } else {
                        showToast("Running is now disabled.");
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        viewModel.isCyclingChecked().observe(getViewLifecycleOwner(), isChecked -> {
            try {
                if (isChecked != prevCyclingChecked) {
                    updateGraph();
                    prevCyclingChecked = isChecked;
                    if (isChecked) {
                        showToast("Cycling is now enabled.");
                    } else {
                        showToast("Cycling is now disabled.");
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        checkBoxDistance.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                updateGraph();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (isChecked && Boolean.FALSE.equals(viewModel.isDistanceChecked().getValue())) {
                weeklyGraphView.setDataType(WeeklyGraphView.GRAPH_DATE_TYPE_DISTANCE);
                viewModel.setCaloriesChecked(false);
                try {
                    updateGraph();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            viewModel.setDistanceChecked(isChecked);
        });

        checkboxCalories.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                updateGraph();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (isChecked && Boolean.FALSE.equals(viewModel.isCaloriesChecked().getValue())) {
                weeklyGraphView.setDataType(WeeklyGraphView.GRAPH_DATE_TYPE_CALORIES);
                viewModel.setDistanceChecked(false);
                try {
                    updateGraph();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            viewModel.setCaloriesChecked(isChecked);
        });


        // Initial update
        try {
            updateGraph();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return binding.getRoot();
    }

    /**
     * Retrieves a list of dates within the last week from the given list of trips.
     *
     * @param trips The list of trips to extract dates from.
     * @return A list of dates within the last week.
     */
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

    /**
     * Displays a toast message with the specified message.
     *
     * @param message The message to display in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        try {
            updateGraph();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Filters the list of trips to include only those within the last week.
     *
     * @param trips The list of trips to filter.
     * @return A list of trips within the last week.
     */
    public List<Trip> filterTripsLastWeek(List<Trip> trips) {
        List<Trip> tripsLastWeek = new ArrayList<>();
        long oneWeekAgoMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds

        for (Trip trip : trips) {
            if (trip.getDate().getTime() >= oneWeekAgoMillis) {
                tripsLastWeek.add(trip);
            }
        }
        return tripsLastWeek;
    }

    /**
     * Calculates the distance covered by trips for each day and stores the result in a map.
     *
     * @param trips The list of walking trips to calculate distances for.
     * @return A map containing daily distances.
     */
    public Map<String, Double> calculateDistanceByDay(List<Trip> trips) {
        Map<String, Double> walkingDistanceByDay = new TreeMap<>(); // TreeMap for sorting by day

        for (Trip trip : trips) {
            String day = getDayFromDate(trip.getDate());
            double distance = trip.getDistance();

            // Update the total distance for the day
            walkingDistanceByDay.put(day, walkingDistanceByDay.getOrDefault(day, 0.0) + distance);
        }

        return walkingDistanceByDay;
    }

    /**
     * Converts a map of data to a list of floats.
     *
     * @param data The map of data to convert.
     * @return A list of floats representing the data values.
     */
    private List<Float> convertMapToList(Map<String, Double> data) {
        List<Float> dataList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataList.add(entry.getValue().floatValue());
        }
        return dataList;
    }

    /**
     * Formats a date as a day string.
     *
     * @param date The date to format.
     * @return A string representing the day portion of the date.
     */
    private String getDayFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        return sdf.format(date);
    }

    /**
     * Updates the graph and UI based on user selections and trip data.
     *
     * @throws ParseException If there is an issue parsing date data.
     */
    private void updateGraph() throws ParseException {
        tripRepository.loadTripHistory().observe(getViewLifecycleOwner(), tripHistory -> {
            // Since we are already on the UI thread, we don't need executors or handlers here
            List<Trip> selectedTrips = filterTripsByMovementType(tripHistory);

            // Filter trips within the last week
            List<Trip> selectedTripsLastWeek = filterTripsLastWeek(selectedTrips);

            // Calculate and set the average speed for each movement type
            viewModel.setAvgWalkSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_WALK));
            viewModel.setAvgRunSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_RUN));
            viewModel.setAvgCycleSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_CYCLE));

            Set<String> selectedMovementTypes = new HashSet<>();
            if (Boolean.TRUE.equals(viewModel.isWalkingChecked().getValue())) {
                selectedMovementTypes.add("Walking");
            }
            if (Boolean.TRUE.equals(viewModel.isRunningChecked().getValue())) {
                selectedMovementTypes.add("Running");
            }
            if (Boolean.TRUE.equals(viewModel.isCyclingChecked().getValue())) {
                selectedMovementTypes.add("Cycling");
            }
            if (checkBoxDistance.isChecked() && !checkboxCalories.isChecked()) {
                // Only display distance data
                Map<String, Double> distanceByDay = calculateDistanceByDay(selectedTrips);
                weeklyGraphView.setDataPoints(
                        convertMapToList(distanceByDay),
                        getDateListLastWeek(selectedTrips)
                );
            } else if (checkboxCalories.isChecked() && !checkBoxDistance.isChecked()) {
                // Only display calorie data
                Map<String, Double> caloriesByDay = calculateCaloriesByDay(selectedTrips);
                weeklyGraphView.setDataPoints(
                        convertMapToList(caloriesByDay),
                        getDateListLastWeek(selectedTrips)
                );
            }
        });
    }

    private boolean shouldIncludeTrip(Trip trip) {
        boolean walkingChecked = Boolean.TRUE.equals(viewModel.isWalkingChecked().getValue());
        boolean runningChecked = Boolean.TRUE.equals(viewModel.isRunningChecked().getValue());
        boolean cyclingChecked = Boolean.TRUE.equals(viewModel.isCyclingChecked().getValue());
        switch (trip.getMovementType()) {
            case Trip.MOVEMENT_WALK:
                return walkingChecked;
            case Trip.MOVEMENT_RUN:
                return runningChecked;
            case Trip.MOVEMENT_CYCLE:
                return cyclingChecked;
            default:
                return false;
        }
    }

    private List<Trip> filterTripsByMovementType(List<Trip> tripHistory) {
        List<Trip> selectedTrips = new ArrayList<>();
        // Filter the trips based on checkbox selections
        for (Trip trip : tripHistory) {
            if (shouldIncludeTrip(trip)) {
                selectedTrips.add(trip);
            }
        }
        return selectedTrips;
    }

    /**
     * Calculates the calories burned for each day based on trips and stores the result in a map.
     *
     * @param trips The list of trips to calculate calories for.
     * @return A map containing daily calorie values.
     */
    private Map<String, Double> calculateCaloriesByDay(List<Trip> trips) {
        Map<String, Double> caloriesByDay = new TreeMap<>();

        for (Trip trip : trips) {
            String day = getDayFromDate(trip.getDate());
            double calories = trip.getCaloriesBurned();

            // Update the total calories for the day
            caloriesByDay.put(day, caloriesByDay.getOrDefault(day, 0.0) + calories);
        }
        return caloriesByDay;
    }

}
