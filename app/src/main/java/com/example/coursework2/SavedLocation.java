package com.example.coursework2;

import com.google.android.gms.maps.model.LatLng;

public class SavedLocation {
    private String name;
    private LatLng latLng; // Use the LatLng class from Google Maps SDK

    public SavedLocation(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
