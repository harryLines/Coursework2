package com.example.trailblazer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * The StepDetector class is an implementation of SensorEventListener specifically
 * designed to detect steps using the device's accelerometer.
 * <p>
 * It calculates steps taken by the user and notifies a StepListener when a step is detected.
 */
public class StepDetector implements SensorEventListener {

    private static final float STEP_THRESHOLD = 12.0f; // Adjust this threshold based on your device and user's walking pattern
    private static final int STEP_DELAY_MILLIS = 500; // Minimum time between steps (adjust as needed)
    private long lastStepTimestamp = 0;
    private int stepCount = 0;

    private final StepListener stepListener;

    /**
     * Constructor for StepDetector.
     *
     * @param listener The listener to receive step detection events.
     */
    public StepDetector(StepListener listener) {
        this.stepListener = listener;
    }

    /**
     * Returns the total number of steps detected since the sensor was activated.
     *
     * @return The total step count.
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Called when there is a new sensor event.
     *
     * @param event The SensorEvent.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float acceleration = calculateAcceleration(values[0], values[1], values[2]);

        // Check for a step using simple peak detection
        if (isStep(acceleration)) {
            handleStep();
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     *
     * @param sensor The Sensor whose accuracy changed.
     * @param accuracy The new accuracy of this sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    /**
     * Calculates the acceleration based on x, y, and z values.
     *
     * @param x The x value of acceleration.
     * @param y The y value of acceleration.
     * @param z The z value of acceleration.
     * @return The calculated acceleration.
     */
    private float calculateAcceleration(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Determines whether the given acceleration indicates a step.
     *
     * @param acceleration The current acceleration value.
     * @return true if a step is detected, false otherwise.
     */
    private boolean isStep(float acceleration) {
        long currentTime = System.currentTimeMillis();

        // Check if the acceleration exceeds the threshold
        if (acceleration > STEP_THRESHOLD) {
            // Check if enough time has passed since the last step
            if (currentTime - lastStepTimestamp > STEP_DELAY_MILLIS) {
                lastStepTimestamp = currentTime;
                return true;
            }
        }

        return false;
    }

    /**
     * Handles the step detection.
     */
    private void handleStep() {
        // Increment the step count
        stepCount++;

        // Notify the listener about the step
        if (stepListener != null) {
            stepListener.onStepDetected(stepCount);
        }
    }

    /**
     * Interface for receiving notifications when a step is detected.
     */
    public interface StepListener {
        /**
         * Called when a step is detected.
         *
         * @param stepCount The current step count.
         */
        void onStepDetected(int stepCount);
    }
}
