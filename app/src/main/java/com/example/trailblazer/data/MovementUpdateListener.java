package com.example.trailblazer.data;

import android.location.Location;

public interface MovementUpdateListener {
    void onMovementUpdate(double distance, Location location);
}