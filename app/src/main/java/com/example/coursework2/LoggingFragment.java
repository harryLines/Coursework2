package com.example.coursework2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
    RadioGroup movementTypeRadioBtnGroup;
    Button btnStartTracking;
    TextView textViewNearbySavedLocation;
    TextClock textClock;
    TextView textViewSteps;
    int steps = 0;
    long seconds = 0;
    double distance = 0.0;
    private RecyclerView recyclerViewReminders;
    String savedLocationName;
    String savedLocationReminders;
    private ReminderAdapter reminderAdapter;


    public LoggingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logging_fragment, container, false);
        // Find the "Start Tracking" button
        btnStartTracking = view.findViewById(R.id.btnStartTracking);
        walkingRadioButton = view.findViewById(R.id.radioBtnWalking);
        runningRadioButton = view.findViewById(R.id.radioBtnRunning);
        cyclingRadioButton = view.findViewById(R.id.radioBtnCycling);
        currentDistanceTxtView = view.findViewById(R.id.txtViewDistance);
        movementTypeRadioBtnGroup = view.findViewById(R.id.trackingTypeGroup);
        textViewNearbySavedLocation = view.findViewById(R.id.txtViewNearbySavedLocation);
        textClock = view.findViewById(R.id.textClockElapsedTime);
        textViewSteps = view.findViewById(R.id.textViewSteps);
        recyclerViewReminders = view.findViewById(R.id.recyclerViewReminders);
        reminderAdapter = new ReminderAdapter(new ArrayList<>());
        recyclerViewReminders.setAdapter(reminderAdapter);

        // Set a click listener for the button
        btnStartTracking.setOnClickListener(v -> toggleService());
        return view;
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

        if(walkingRadioButton.isChecked()) {
            serviceIntent.putExtra("movementType", 0);
        } else if (runningRadioButton.isChecked()) {
            serviceIntent.putExtra("movementType", 1);
        } else if (cyclingRadioButton.isChecked()) {
            serviceIntent.putExtra("movementType", 2);
        } else {
            showMovementTypeAlert();
            return false; // Stop further execution
        }

        getActivity().startService(serviceIntent);
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
                distance = intent.getDoubleExtra("distance", distance);
                seconds = intent.getLongExtra("trackingDuration", seconds);
                if(intent.getStringExtra("savedLocationName") != null) {
                    savedLocationName = intent.getStringExtra("savedLocationName");
                }
                if(intent.getStringExtra("savedLocationReminders") != null) {
                    savedLocationReminders = intent.getStringExtra("savedLocationReminders");
                }
                if (Objects.equals(savedLocationName, "NULL")) {
                    textViewNearbySavedLocation.setText(R.string.you_are_not_nearby_any_saved_locations);
                    // Update the adapter data
                    reminderAdapter.setReminders(new ArrayList<>());
                } else if(savedLocationName != null){
                    textViewNearbySavedLocation.setText(String.format("%s%s", getString(R.string.you_are_currently_at), savedLocationName));
                    // Get the list of reminders
                    Log.d("REMINDERS",savedLocationReminders);
                    List<String> reminderList = Arrays.asList(savedLocationReminders.split(","));

                    // Update the adapter data
                    reminderAdapter.setReminders(reminderList);
                }
                currentDistanceTxtView.setText(String.format(Locale.UK, "Distance: %.2f meters", distance));
                steps = intent.getIntExtra("stepCount", steps);
                textViewSteps.setText(String.valueOf(steps));

                // Convert elapsed time to a custom format (hh:mm:ss)
                String elapsedTime = formatElapsedTime(seconds);
                textClock.setText(elapsedTime);
            }
        }
    };

    private String formatElapsedTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver when the fragment is resumed
        IntentFilter filter = new IntentFilter(MovementTrackerService.ACTION_DISTANCE_UPDATE);
        requireActivity().registerReceiver(movementUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver when the fragment is paused
        requireActivity().unregisterReceiver(movementUpdateReceiver);
    }

}
