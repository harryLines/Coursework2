package com.example.TrailBlazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.TrailBlazer.databinding.HomeFragmentBinding;

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
    HomeFragmentViewModel viewModel;
    public HomeFragment() {
    }

    private List<Trip> loadTripHistory() throws ParseException {
        // Use the DatabaseManager to load trip history from the SQLite database
        DatabaseManager databaseManager = new DatabaseManager(getContext());
        return databaseManager.loadTripHistory();
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

        HomeFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false);

        // Create an instance of your ViewModel
        viewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);

        // Set the ViewModel to the binding
        binding.setViewModel(viewModel);

        // Set the lifecycle owner for LiveData observation
        binding.setLifecycleOwner(this);

        checkboxWalking = binding.checkBoxWalking;
        checkboxRunning = binding.checkBoxRunning;
        checkboxCycling = binding.checkBoxCycling;
        txtViewAvgWalkSpeed = binding.textViewWalkingSpeed;
        txtViewAvgRunSpeed = binding.textViewRunningSpeed;
        txtViewAvgCycleSpeed = binding.textViewCyclingSpeed;
        weeklyGraphView = binding.weeklyGraphView;

        viewModel.setWalkingChecked(true); // Set initial value for walking checkbox
        viewModel.setRunningChecked(false); // Set initial value for running checkbox
        viewModel.setCyclingChecked(false);

        // Load trip history data
        try {
            List<Trip> tripHistory = loadTripHistory();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Set a listener to handle checkbox selections
        checkboxWalking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                updateGraph(weeklyGraphView);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        checkboxRunning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                updateGraph(weeklyGraphView);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        checkboxCycling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                updateGraph(weeklyGraphView);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        // Initial update
        try {
            updateGraph(weeklyGraphView);
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

    @Override
    public void onResume() {
        super.onResume();
        try {
            updateGraph(weeklyGraphView);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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

    private void updateGraph(WeeklyGraphView weeklyGraphView) throws ParseException {
        List<Trip> selectedTrips = new ArrayList<>();
        List<Trip> tripHistory = loadTripHistory();
        // Find the selected checkboxes

        // Filter the trips based on checkbox selections
        for (Trip trip : tripHistory) {
            switch (trip.getMovementType()) {
                case Trip.MOVEMENT_WALK:
                    if (viewModel.isWalkingChecked().getValue()) {
                        selectedTrips.add(trip);
                    }
                    break;
                case Trip.MOVEMENT_RUN:
                    if (viewModel.isRunningChecked().getValue()) {
                        selectedTrips.add(trip);
                    }
                    break;
                case Trip.MOVEMENT_CYCLE:
                    if (viewModel.isCyclingChecked().getValue()) {
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
        viewModel.setAvgWalkSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_WALK));
        viewModel.setAvgRunSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_RUN));
        viewModel.setAvgCycleSpeed(calculateAverageSpeed(selectedTripsLastWeek, Trip.MOVEMENT_CYCLE));
    }
}
