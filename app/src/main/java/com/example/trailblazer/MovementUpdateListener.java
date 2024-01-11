package com.example.trailblazer;

import android.location.Location;

public interface MovementUpdateListener {
    void onMovementUpdate(double distance, Location location);
}