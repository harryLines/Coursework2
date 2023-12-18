package com.example.trailblazer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
public class StepDetector implements SensorEventListener {

    private static final float STEP_THRESHOLD = 12.0f; // Adjust this threshold based on your device and user's walking pattern
    private static final int STEP_DELAY_MILLIS = 500; // Minimum time between steps (adjust as needed)

    private long lastStepTimestamp = 0;

    public int getStepCount() {
        return stepCount;
    }

    private int stepCount = 0;

    private StepListener stepListener;

    public StepDetector(StepListener listener) {
        this.stepListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float acceleration = calculateAcceleration(values[0], values[1], values[2]);

        // Check for a step using a simple peak detection algorithm
        if (isStep(acceleration)) {
            handleStep();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    private float calculateAcceleration(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

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

    private void handleStep() {
        // Increment the step count
        stepCount++;

        // Notify the listener about the step
        if (stepListener != null) {
            stepListener.onStepDetected(stepCount);
        }
    }

    // Interface for StepListener
    public interface StepListener {
        void onStepDetected(int stepCount);
    }
}
