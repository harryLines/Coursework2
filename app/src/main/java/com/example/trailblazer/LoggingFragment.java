package com.example.trailblazer;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.trailblazer.databinding.LoggingFragmentBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggingFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
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
    Spinner spinnerWeather;
    ImageButton btnAddPhoto;
    private ReminderAdapter reminderAdapter;
    private LoggingFragmentViewModel viewModel;
    private Database database;
    ActivityResultLauncher<String> requestCameraPermissionLauncher;
    ActivityResultLauncher<Void> cameraLauncher;

    public LoggingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LoggingFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.logging_fragment, container, false);

        database = DatabaseManager.getInstance(requireContext());
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
        spinnerWeather = binding.spinnerWeather;
        btnAddPhoto = binding.btnAddPhoto;

        String[] values = getResources().getStringArray(R.array.weather_array);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, values);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerWeather.setAdapter(adapter);

        // Set a listener to handle item selection
        spinnerWeather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item
                String selectedValue = values[position];
                viewModel.setWeather(position);
                MovementTrackerService.updateWeather(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        reminderAdapter = new ReminderAdapter(new ArrayList<>());
        recyclerViewReminders.setAdapter(reminderAdapter);
        btnStartTracking.setOnClickListener(v -> toggleService());
        btnAddPhoto.setOnClickListener(v -> checkCameraPermission());

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission granted, launch the camera
                        takePicture();
                    } else {
                        // Permission denied
                        Toast.makeText(getContext(), "Camera permission is required for image capture", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(), result -> {
                    if (result != null) {
                        // Handle the captured image
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), result);
                        btnAddPhoto.setBackground(bitmapDrawable);
                        byte[] imageByteArray = convertBitmapToByteArray(result);
                        MovementTrackerService.updateImage(saveImageToFile(imageByteArray));
                    } else {
                        // Handle the case where the user canceled the capture
                        Toast.makeText(getContext(), "Image capture canceled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        return binding.getRoot();
    }

    private void checkCameraPermission() {
        // Check if camera permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, launch the camera
            takePicture();
        } else {
            // Request camera permission
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void takePicture() {
        // Launch the camera to capture an image
        cameraLauncher.launch(null);
    }

    private String saveImageToFile(byte[] imageData) {
        // Get the directory for the app's private pictures directory.
        File directory = new File(getActivity().getFilesDir(), "images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create a unique file name based on the timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + ".jpg";

        // Create the file in the specified directory
        File imageFile = new File(directory, imageFileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(imageData);
            return imageFile.getAbsolutePath();  // Return the file path
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null;
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
            // The service is not running, so check for location permission
            if (checkLocationPermission()) {
                // Location permission granted, start the service
                if (startService()) {
                    btnStartTracking.setText("Stop Tracking");

                    // Make radio buttons invisible
                    viewModel.setIsTracking(true);
                }
            } else {
                // Location permission not granted, show a dialog requesting permission
                showLocationPermissionDialog();
            }
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Location Permissions Required")
                .setMessage("Location permissions are needed to start tracking. Please grant the location permission in the app settings.")
                .setPositiveButton("Grant Permissions", (dialog, which) -> {
                    // Open app settings to allow the user to grant permissions
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle the case where the user cancels the permission request
                    Toast.makeText(getContext(), "Tracking requires location permissions.", Toast.LENGTH_SHORT).show();
                })
                .show();
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

    private List<Goal> updateProgress(List<Goal> currentGoals, double burnedCalories, double distanceCovered, int stepsTaken) {
        if (currentGoals != null) {

            // Iterate through the goals and update them
            for (Goal goal : currentGoals) {
                switch (goal.getMetricType()) {
                    case Goal.METRIC_CALORIES:
                        goal.setProgress(goal.getProgress() + burnedCalories);
                        break;
                    case Goal.METRIC_KILOMETERS:
                        goal.setProgress(goal.getProgress() + distanceCovered);
                        break;
                    case Goal.METRIC_STEPS:
                        goal.setProgress(goal.getProgress() + stepsTaken);
                        break;
                    default:
                }
                if (goal.getProgress() >= goal.getTarget()) {
                    goal.setComplete();
                }
            }
        }
        return currentGoals;
    }

    private void updateGoals() {
        double burnedCalories = viewModel.getCalories().getValue();
        double distanceCovered = viewModel.getDistance().getValue(); // Assuming distance is the metric for kilometers goal
        int stepsTaken = viewModel.getSteps().getValue();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Goal> currentGoals = database.goalDao().loadGoals();
            updateProgress(currentGoals,burnedCalories,distanceCovered,stepsTaken);
            database.goalDao().updateGoals(currentGoals);
            handler.post(() -> {
            });
        });
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
        btnAddPhoto.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_design));    }

    // Method to start the service
    private boolean startService() {
        // Create an intent for your service
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);

        if (Boolean.TRUE.equals(viewModel.getWalkingChecked().getValue())) {
            serviceIntent.putExtra("movementType", 0);
        } else if (Boolean.TRUE.equals(viewModel.getRunningChecked().getValue())) {
            serviceIntent.putExtra("movementType", 1);
        } else if (Boolean.TRUE.equals(viewModel.getCyclingChecked().getValue())) {
            serviceIntent.putExtra("movementType", 2);
        } else {
            showMovementTypeAlert();
            return false; // Stop further execution
        }

        requireActivity().startForegroundService(serviceIntent);
        return true;
    }

    // Method to stop the service
    private void stopService() {
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);
        requireActivity().stopService(serviceIntent);
    }

    // Method to check if a service is running
    @SuppressWarnings("deprecation")
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
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

    private final BroadcastReceiver movementUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.requireNonNull(intent.getAction()).equals(MovementTrackerService.ACTION_DISTANCE_UPDATE)) {
                viewModel.setDistance(intent.getDoubleExtra("distance", viewModel.getDistance().getValue()));
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
                        List<String> reminderList = Arrays.asList(Objects.requireNonNull(savedLocationReminders).split(","));
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
