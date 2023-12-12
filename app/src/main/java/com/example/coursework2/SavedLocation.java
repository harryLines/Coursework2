package com.example.coursework2;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SavedLocation {
    private String name;
    private LatLng latLng;
    private List<String> reminders;

    public SavedLocation(String name, LatLng latLng,List<String> reminders) {
        this.name = name;
        this.latLng = latLng;
        if(reminders == null) {
            this.reminders = new ArrayList<>();
        } else {
            this.reminders = reminders;
        }
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
