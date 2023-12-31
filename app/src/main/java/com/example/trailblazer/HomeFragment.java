package com.example.trailblazer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trailblazer.databinding.HomeFragmentBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    CheckBox checkboxWalking;
    CheckBox checkboxRunning;
    CheckBox checkboxCycling;
    CheckBox checkBoxDistance;
    CheckBox checkboxCalories;
    TextView txtViewAvgWalkSpeed;
    TextView txtViewAvgRunSpeed;
    TextView txtViewAvgCycleSpeed;
    WeeklyGraphViewDistance weeklyGraphViewDistance;
    WeeklyGraphViewCalories weeklyGraphViewCalories;
    HomeFragmentViewModel viewModel;
    HomeFragmentBinding binding;
    private boolean prevWalkingChecked = true;
    private boolean prevRunningChecked = false;
    private boolean prevCyclingChecked = false;
    private DatabaseManager databaseManager;
    private Database database;
    TripDao tripDao;
    public HomeFragment() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);
        database = DatabaseManager.getInstance(requireContext());
        tripDao = database.tripDao();
        // Create an instance of your ViewModel
        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);

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
        weeklyGraphViewDistance = binding.weeklyGraphViewDistance;
        weeklyGraphViewCalories = binding.weeklyGraphViewCalories;

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
            if (isChecked && Boolean.FALSE.equals(viewModel.isDistanceChecked().getValue())) {
                weeklyGraphViewDistance.setVisibility(View.VISIBLE);
                weeklyGraphViewCalories.setVisibility(View.GONE);
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
            if (isChecked && Boolean.FALSE.equals(viewModel.isCaloriesChecked().getValue())) {
                weeklyGraphViewDistance.setVisibility(View.GONE);
                weeklyGraphViewCalories.setVisibility(View.VISIBLE);
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            updateGraph();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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

    private void updateGraph() throws ParseException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Trip> tripHistory = tripDao.loadTripHistory();
            List<Trip> selectedTrips = new ArrayList<>();

            // Filter the trips based on checkbox selections
            for (Trip trip : tripHistory) {
                switch (trip.getMovementType()) {
                    case Trip.MOVEMENT_WALK:
                        if (Boolean.TRUE.equals(viewModel.isWalkingChecked().getValue())) {
                            selectedTrips.add(trip);
                        }
                        break;
                    case Trip.MOVEMENT_RUN:
                        if (Boolean.TRUE.equals(viewModel.isRunningChecked().getValue())) {
                            selectedTrips.add(trip);
                        }
                        break;
                    case Trip.MOVEMENT_CYCLE:
                        if (Boolean.TRUE.equals(viewModel.isCyclingChecked().getValue())) {
                            selectedTrips.add(trip);
                        }
                        break;
                }
            }

            // Filter trips within the last week
            List<Trip> selectedTripsLastWeek = filterTripsLastWeek(selectedTrips);

            handler.post(() -> {
                // UI Thread work here

                if (checkBoxDistance.isChecked()) {
                    // Summarize distance and time for selected trips by day
                    Map<String, Double> distanceByDay = calculateDistanceByDay(selectedTripsLastWeek);

                    // Set the data to the WeeklyGraphView
                    List<Date> dateListLastWeek = getDateListLastWeek(selectedTripsLastWeek);
                    weeklyGraphViewDistance.setDataPoints(
                            convertMapToList(distanceByDay),
                            dateListLastWeek
                    );

                    Collections.reverse(dateListLastWeek);
                } else {
                    // Summarize calorie values for selected trips by day
                    Map<String, Double> caloriesByDay = calculateCaloriesByDay(selectedTripsLastWeek);

                    // Set the data to the WeeklyGraphViewCalories
                    List<Date> dateListLastWeek = getDateListLastWeek(selectedTripsLastWeek);
                    weeklyGraphViewCalories.setDataPoints(
                            convertMapToListInt(caloriesByDay),
                            dateListLastWeek
                    );

                    Collections.reverse(dateListLastWeek);
                }

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
            });
        });
    }

    private List<Integer> convertMapToListInt(Map<String, Double> data) {
        List<Integer> dataList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataList.add(entry.getValue().intValue());
        }
        return dataList;
    }


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
