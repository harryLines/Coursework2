package com.example.trailblazer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;
@Dao
public interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE location_id = :locationId")
    LiveData<List<Reminder>> loadRemindersForLocation(long locationId);
    @Query("SELECT * FROM reminders r INNER JOIN saved_locations s ON r.location_id = s._id WHERE s.name = :locationName")
    LiveData<List<Reminder>> loadRemindersForLocationName(String locationName);
    @Query("SELECT * FROM reminders")
    LiveData<List<Reminder>> loadReminders();
    @Query("DELETE FROM reminders WHERE _id = :reminderID")
    void removeReminder(long reminderID);
    @Query("DELETE FROM reminders")
    void deleteReminders();
    @Insert
    long addNewReminder(Reminder reminder);
    @Query("SELECT EXISTS (SELECT 1 FROM reminders WHERE location_id = :locationId AND reminder_text = :reminder)")
    boolean reminderExists(long locationId, String reminder);
    @RawQuery(observedEntities = Reminder.class)
    List<Reminder> loadRemindersWithSelection(SupportSQLiteQuery query);
}
