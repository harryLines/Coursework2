package com.example.trailblazer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "reminders")
@TypeConverters({Converters.class})
public class Reminder {
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public long getLocationID() {
        return locationID;
    }
    public void setLocationID(long locationID) {
        this.locationID = locationID;
    }
    public String getReminderText() {
        return reminderText;
    }
    public void setReminderText(String reminderText) {
        this.reminderText = reminderText;
    }
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;
    @ColumnInfo(name = "location_id")
    private long locationID;
    @ColumnInfo(name = "reminder_text")
    private String reminderText;
    public Reminder(long locationID, String reminderText) {
        this.locationID = locationID;
        this.reminderText = reminderText;
    }
}
