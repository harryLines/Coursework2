package com.example.trailblazer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SavedLocation {
    long locationID;
    private String name;
    private LatLng latLng;
    private List<String> reminders;
    private boolean entered;
    public long getLocationID() {
        return locationID;
    }
    public void setLocationID(long locationID) {
        this.locationID = locationID;
    }
    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public SavedLocation(long locationID,String name, LatLng latLng,List<String> reminders) {
        this.name = name;
        this.latLng = latLng;
        if(reminders == null) {
            this.reminders = new ArrayList<>();
        } else {
            this.reminders = reminders;
        }
        this.locationID = locationID;
        this.entered = false;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public List<String> getReminders() {
        return reminders;
    }

    public void addReminder(String reminder) {
        reminders.add(reminder);
    }

    public void setReminders(List<String> reminders) {
        this.reminders = reminders;
    }

    public String getRemindersAsString() {
        StringBuilder remindersString = new StringBuilder();
        for (String reminder : reminders) {
            remindersString.append(reminder).append(",");
        }
        return remindersString.toString();
    }
}
