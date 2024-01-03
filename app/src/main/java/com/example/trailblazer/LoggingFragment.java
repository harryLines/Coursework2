package com.example.trailblazer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.trailblazer.databinding.LoggingFragmentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LoggingFragment extends Fragment {
    RadioButton walkingRadioButton;
    RadioButton runningRadioButton;
    RadioButton cyclingRadioButton;
    TextView currentDistanceTxtView;
    TextView textViewTravelType;
    RadioGroup movementTypeRadioBtnGroup;
    Button btnStartTracking;
    TextView textViewNearbySavedLocation;
    TextView textClock;
    TextView textViewSteps;
    TextView textViewCaloriesBurned;
    String savedLocationReminders;
    private ReminderAdapter reminderAdapter;
    private LoggingFragmentViewModel viewModel;
    private DatabaseManager dbManager;

    public LoggingFragment(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LoggingFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.logging_fragment, container, false);

        viewModel = new ViewModelProvider(this).get(LoggingFragmentViewModel.class);

        // Bind the ViewModel to the layout
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        btnStartTracking = binding.btnStartTracking;
        walkingRadioButton = binding.radioBtnWalking;
        runningRadioButton = binding.radioBtnRunning;
        cyclingRadioButton = binding.radioBtnCycling;
        currentDistanceTxtView = binding.txtViewDistance;
        movementTypeRadioBtnGroup = binding.trackingTypeGroup;
        textViewNearbySavedLocation = binding.txtViewNearbySavedLocation;
        textClock = binding.textViewElapsedTime;
        textViewSteps = binding.textViewSteps;
        textViewTravelType = binding.textViewTravelType;
        textViewCaloriesBurned = binding.textViewCalories;
        RecyclerView recyclerViewReminders = binding.recyclerViewReminders;

        reminderAdapter = new ReminderAdapter(new ArrayList<>());
        recyclerViewReminders.setAdapter(reminderAdapter);
        btnStartTracking.setOnClickListener(v -> toggleService());

        return binding.getRoot();
    }

    // Method to start the service
    private void toggleService() {
        // Check if the service is already running
        if (isServiceRunning()) {
            // The service is running, so stop it
            updateGoals();
            stopService();
            showStopTrackingDialog();
            btnStartTracking.setText("Start Tracking");

            // Make radio buttons visible
            viewModel.setIsTracking(false);

            resetValues();

        } else {
            // The service is not running, so start it
            if (startService()) {
                btnStartTracking.setText("Stop Tracking");

                // Make radio buttons invisible
                viewModel.setIsTracking(true);

            }
        }
    }


    private void showStopTrackingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Tracking Stopped")
                .setMessage(getCurrentValuesMessage())
                .setPositiveButton("OK", (dialog, which) -> {
                    resetValues();
                })
                .show();
    }

    private void updateGoals() {
        List<Goal> currentGoals = dbManager.loadGoals();

        if (currentGoals != null) {
            double burnedCalories = viewModel.getCalories().getValue();
            double distanceCovered = viewModel.getDistance().getValue(); // Assuming distance is the metric for kilometers goal
            int stepsTaken = viewModel.getSteps().getValue(); // Assuming steps is the metric for steps goal

            // Iterate through the goals and update them
            for (Goal goal : currentGoals) {
                switch (goal.getMetricType()) {
                    case Goal.METRIC_CALORIES:
                        goal.setProgress(goal.getProgress() + burnedCalories);
                        Log.d("Goal Update", "Calories goal updated. New progress: " + goal.getProgress() + "ADDED:");
                        break;
                    case Goal.METRIC_KILOMETERS:
                        goal.setProgress(goal.getProgress() + distanceCovered);
                        Log.d("Goal Update", "Distance goal updated. New progress: " + goal.getProgress());
                        break;
                    case Goal.METRIC_STEPS:
                        goal.setProgress(goal.getProgress() + stepsTaken);
                        Log.d("Goal Update", "Steps goal updated. New progress: " + goal.getProgress());
                        break;
                    default:
                        Log.w("Goal Update", "Unsupported metric type: " + goal.getMetricType());
                }
                if (goal.getProgress() >= goal.getTarget()) {
                    goal.setComplete();
                }
            }

            // Save the updated goals back to the database
            dbManager.updateGoals(currentGoals);
            Log.d("Goal Update", "Goals updated and saved to the database.");
        }
    }


    private String getCurrentValuesMessage() {
        String distance = "Distance: " + viewModel.getDistance().getValue() + " km\n";
        String duration = "Duration: " + formatTime(viewModel.getSeconds().getValue()) + "\n";
        String steps = "Steps: " + viewModel.getSteps().getValue() + "\n";
        String calories = "Calories Burned: " + viewModel.getCalories().getValue() + " kcal\n";
        return distance + duration + steps + calories;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private void resetValues() {
        viewModel.setDistance(0.0);
        viewModel.setSeconds(0);
        viewModel.setSteps(0);
        viewModel.setCalories(0);
        viewModel.setSavedLocationName("");
        reminderAdapter.setReminders(new ArrayList<>());
    }

    // Method to start the service
    private boolean startService() {
        // Create an intent for your service
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);

        if (viewModel.getWalkingChecked().getValue()) {
            serviceIntent.putExtra("movementType", 0);
        } else if (viewModel.getRunningChecked().getValue()) {
            serviceIntent.putExtra("movementType", 1);
        } else if (viewModel.getCyclingChecked().getValue()) {
            serviceIntent.putExtra("movementType", 2);
        } else {
            showMovementTypeAlert();
            return false; // Stop further execution
        }

        getActivity().startForegroundService(serviceIntent);
        return true;
    }

    // Method to stop the service
    private void stopService() {
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);
        getActivity().stopService(serviceIntent);
    }

    // Method to check if a service is running
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MovementTrackerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showMovementTypeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert")
                .setMessage("You need to select a movement type.")
                .setPositiveButton("OK", null)
                .show();
    }

    private BroadcastReceiver movementUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MovementTrackerService.ACTION_DISTANCE_UPDATE)) {
                viewModel.setDistance(intent.getDoubleExtra("distance", viewModel.getDistance().getValue()));
                Log.d("DISTANCE", String.valueOf(viewModel.getDistance().getValue()));
                viewModel.setSeconds(intent.getLongExtra("trackingDuration", viewModel.getSeconds().getValue()));
                viewModel.setSteps(intent.getIntExtra("stepCount", viewModel.getSteps().getValue()));
                viewModel.setCalories(intent.getIntExtra("caloriesBurned", viewModel.getCalories().getValue()));

                if (Objects.equals(intent.getStringExtra("savedLocationName"), "NULL")) {
                    textViewNearbySavedLocation.setText(R.string.you_are_not_nearby_any_saved_locations);
                    reminderAdapter.setReminders(new ArrayList<>());
                } else {
                    if (intent.getStringExtra("savedLocationName") != null) {
                        viewModel.setSavedLocationName(intent.getStringExtra("savedLocationName"));
                    }
                    if (intent.getStringExtra("savedLocationReminders") != null) {
                        savedLocationReminders = intent.getStringExtra("savedLocationReminders");
                        List<String> reminderList = Arrays.asList(savedLocationReminders.split(","));
                        reminderAdapter.setReminders(reminderList);
                    }
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver when the fragment is resumed
        IntentFilter filter = new IntentFilter(MovementTrackerService.ACTION_DISTANCE_UPDATE);
        btnStartTracking.setText(isServiceRunning() ? "Stop Tracking" : "Start Tracking");
        requireActivity().registerReceiver(movementUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver when the fragment is paused
        requireActivity().unregisterReceiver(movementUpdateReceiver);
    }
}
