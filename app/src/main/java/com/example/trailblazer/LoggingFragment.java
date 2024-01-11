package com.example.trailblazer;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.databinding.LoggingFragmentBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A Fragment class responsible for logging walking, running, and cycling.
 * It allows users to track their progress, progress in their goals, and capture photos during activities.
 */
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
    ActivityResultLauncher<String> requestCameraPermissionLauncher;
    ActivityResultLauncher<Void> cameraLauncher;
    GoalRepository goalRepository;

    public LoggingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LoggingFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.logging_fragment, container, false);

        viewModel = new ViewModelProvider(this).get(LoggingFragmentViewModel.class);
        goalRepository = new GoalRepository(DatabaseManager.getInstance(requireContext()).goalDao());
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

    /**
     * Checks if the required camera permission is granted and takes a picture using the device's camera.
     */
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

    /**
     * Launches the device's camera to capture an image.
     */
    private void takePicture() {
        // Launch the camera to capture an image
        cameraLauncher.launch(null);
    }

    /**
     * Saves an image represented as a byte array to a file in the app's private directory.
     *
     * @param imageData The byte array representing the image data.
     * @return The file path of the saved image.
     */
    public String saveImageToFile(byte[] imageData) {
        // Get the directory for the app's private pictures directory.
        File directory = new File(requireActivity().getFilesDir(), "images");
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

    /**
     * Converts a Bitmap image to a byte array.
     *
     * @param bitmap The Bitmap image to be converted.
     * @return The resulting byte array.
     */
    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Toggles the tracking service on or off based on the current tracking state.
     * Starts or stops the MovementTrackerService.
     */
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

    /**
     * Checks if the location permission is granted.
     *
     * @return True if the location permission is granted, false otherwise.
     */
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Displays a dialog requesting location permissions.
     */
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

    /**
     * Displays a dialog informing the user that tracking has been stopped and provides summary information.
     */
    private void showStopTrackingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Tracking Stopped")
                .setMessage(getCurrentValuesMessage())
                .setPositiveButton("OK", (dialog, which) -> resetValues())
                .show();
    }

    /**
     * Updates the stored fitness goals based on activity data.
     * Called when tracking is stopped.
     */
    private void updateGoals() {
        double burnedCalories = viewModel.getCalories().getValue();
        double distanceCovered = viewModel.getDistance().getValue();
        int stepsTaken = viewModel.getSteps().getValue();

        goalRepository.loadAndUpdateGoals(burnedCalories,distanceCovered,stepsTaken);
    }

    /**
     * Generates a summary message containing current activity values.
     *
     * @return A formatted string containing activity details.
     */
    private String getCurrentValuesMessage() {
        String distance = "Distance: " + viewModel.getDistance().getValue() + " km\n";
        String duration = "Duration: " + formatTime(viewModel.getSeconds().getValue()) + "\n";
        String steps = "Steps: " + viewModel.getSteps().getValue() + "\n";
        String calories = "Calories Burned: " + viewModel.getCalories().getValue() + " kcal\n";
        return distance + duration + steps + calories;
    }

    /**
     * Formats a time duration from seconds to HH:MM:SS format.
     *
     * @param seconds The time duration in seconds.
     * @return The formatted time string.
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    /**
     * Resets activity-related values and UI elements to their initial states.
     */
    private void resetValues() {
        viewModel.setDistance(0.0);
        viewModel.setSeconds(0);
        viewModel.setSteps(0);
        viewModel.setCalories(0);
        viewModel.setSavedLocationName("");
        reminderAdapter.setReminders(new ArrayList<>());
        btnAddPhoto.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.button_design));    }

    /**
     * Starts the MovementTrackerService to track user activities based on the selected movement type.
     *
     * @return True if the service was successfully started, false if there was an issue or no movement type was selected.
     */
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

    /**
     * Stops the MovementTrackerService to halt activity tracking.
     */
    private void stopService() {
        Intent serviceIntent = new Intent(getActivity(), MovementTrackerService.class);
        requireActivity().stopService(serviceIntent);
    }

    /**
     * Checks whether the MovementTrackerService is currently running.
     *
     * @return True if the service is running, false otherwise.
     */
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

    /**
     * Displays an alert dialog to inform the user that a movement type must be selected for tracking.
     */
    private void showMovementTypeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Alert")
                .setMessage("You need to select a movement type.")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * BroadcastReceiver for receiving updates from the MovementTrackerService, such as activity data and location information.
     * Updates the ViewModel and UI elements based on the received data.
     */
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

    /**
     * Called when the fragment is resumed. Registers the movementUpdateReceiver BroadcastReceiver for receiving service updates
     * and updates the UI elements accordingly.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver when the fragment is resumed
        IntentFilter filter = new IntentFilter(MovementTrackerService.ACTION_DISTANCE_UPDATE);
        btnStartTracking.setText(isServiceRunning() ? "Stop Tracking" : "Start Tracking");
        requireActivity().registerReceiver(movementUpdateReceiver, filter);
    }

    /**
     * Called when the fragment is paused. Unregisters the movementUpdateReceiver BroadcastReceiver to stop receiving updates.
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver when the fragment is paused
        requireActivity().unregisterReceiver(movementUpdateReceiver);
    }
}
