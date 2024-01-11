package com.example.trailblazer.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
@Entity(tableName = "saved_locations")
@TypeConverters({Converters.class})
public class SavedLocation {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long locationID;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "latlng")
    private LatLng latLng;
    @Ignore
    private List<Reminder> reminders;
    private boolean entered;
    public SavedLocation(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
        this.reminders = new ArrayList<>(); // Initialize the list
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
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
    public String getName() {
        return name;
    }
    public LatLng getLatLng() {
        return latLng;
    }
    public List<Reminder> getReminders() {
        return reminders;
    }
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
    }
    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }
    /**
     * Retrieves the reminders as a comma-separated string.
     * <p>
     * This method iterates through the list of reminders and appends each reminder's text to a
     * StringBuilder, separated by commas. The resulting string represents all reminders in the list
     * as a single, comma-separated value.
     *
     * @return A comma-separated string containing the reminder texts, or an empty string if there are no reminders.
     */
    public String getRemindersAsString() {
        StringBuilder remindersString = new StringBuilder();
        for (Reminder reminder : reminders) {
            remindersString.append(reminder.getReminderText()).append(",");
        }
        return remindersString.toString();
    }
}
