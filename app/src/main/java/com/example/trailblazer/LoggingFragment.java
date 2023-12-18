package com.example.trailblazer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.databinding.LoggingFragmentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoggingFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    RadioButton walkingRadioButton;
    RadioButton runningRadioButton;
    RadioButton cyclingRadioButton;
    TextView currentDistanceTxtView;
    RadioGroup movementTypeRadioBtnGroup;
    Button btnStartTracking;
    TextView textViewNearbySavedLocation;
    TextView textClock;
    TextView textViewSteps;
    String savedLocationReminders;
    private ReminderAdapter reminderAdapter;
    private LoggingFragmentViewModel viewModel;
    public LoggingFragment() {

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
        textClock = binding.textClockElapsedTime;
        textViewSteps = binding.textViewSteps;
        RecyclerView recyclerViewReminders = binding.recyclerViewReminders;

        reminderAdapter = new ReminderAdapter(new ArrayList<>());
        recyclerViewReminders.setAdapter(reminderAdapter);

        btnStartTracking.setOnClickListener(v -> toggleService());

        return binding.getRoot();
    }

    private void disableRadioGroup(RadioGroup radioGroup) {
        radioGroup.setClickable(false);
        radioGroup.setFocusable(false);

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setClickable(false);
            radioGroup.getChildAt(i).setFocusable(false);
        }
    }

    private void enableRadioGroup(RadioGroup radioGroup) {
        radioGroup.setClickable(true);
        radioGroup.setFocusable(true);

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setClickable(true);
            radioGroup.getChildAt(i).setFocusable(true);
        }
    }

    // Method to start the service
    private void toggleService() {
        // Check if the service is already running
        if (isServiceRunning()) {
            // The service is running, so stop it
            stopService();
            enableRadioGroup(movementTypeRadioBtnGroup);
            btnStartTracking.setText("Start Tracking");
        } else {
            // The service is not running, so start it
            if (startService()) {
                disableRadioGroup(movementTypeRadioBtnGroup);
                btnStartTracking.setText("Stop Tracking");
            }
        }
    }

    // Method to start the service
    private boolean startService() {
        // Create an intent for your service
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);

        if(viewModel.getWalkingChecked().getValue()) {
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

                if (Objects.equals(intent.getStringExtra("savedLocationName"), "NULL")) {
                    textViewNearbySavedLocation.setText(R.string.you_are_not_nearby_any_saved_locations);
                    reminderAdapter.setReminders(new ArrayList<>());
                } else {
                    if(intent.getStringExtra("savedLocationName") != null) {
                        viewModel.setSavedLocationName(intent.getStringExtra("savedLocationName"));
                    }
                    if(intent.getStringExtra("savedLocationReminders") != null) {
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
