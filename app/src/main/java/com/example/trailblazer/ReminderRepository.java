package com.example.trailblazer;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for managing Reminder entities.
 */
public class ReminderRepository {
    private final ReminderDao reminderDao;
    private final Executor executor;

    /**
     * Constructor for ReminderRepository.
     *
     * @param reminderDao DAO for accessing reminder data.
     */
    public ReminderRepository(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves reminders for a specific location.
     *
     * @param locationId The ID of the location.
     * @return A list of reminders for the specified location.
     */
    public LiveData<List<Reminder>> loadRemindersForLocation(long locationId) {
        return reminderDao.loadRemindersForLocation(locationId);
    }

    /**
     * Retrieves reminders for a specific location by name.
     *
     * @param locationName The name of the location.
     * @return A list of reminders for the specified location name.
     */
    public LiveData<List<Reminder>> loadRemindersForLocationName(String locationName) {
        return reminderDao.loadRemindersForLocationName(locationName);
    }

    /**
     * Retrieves all reminders.
     *
     * @return A list of all reminders.
     */
    public LiveData<List<Reminder>> loadReminders() {
        return reminderDao.loadReminders();
    }

    /**
     * Adds a new reminder to the database.
     *
     * @param reminder The reminder to be added.
     */
    public void addNewReminder(Reminder reminder, ReminderInsertCallback callback) {
        executor.execute(() -> {
            long reminderId = reminderDao.addNewReminder(reminder);
            if (callback != null) {
                callback.onReminderInserted(reminderId);
            }
        });
    }

    /**
     * Removes a specific reminder from the database.
     *
     * @param reminderID The ID of the reminder to be removed.
     */
    public void removeReminder(long reminderID) {
        executor.execute(() -> reminderDao.removeReminder(reminderID));
    }

    /**
     * Deletes all reminders from the database.
     */
    public void deleteReminders() {
        executor.execute(reminderDao::deleteReminders);
    }

    /**
     * Checks if a reminder already exists.
     *
     * @param locationId The ID of the location.
     * @param reminder The reminder text.
     * @return True if the reminder exists, false otherwise.
     */
    public boolean reminderExists(long locationId, String reminder) {
        return reminderDao.reminderExists(locationId, reminder);
    }

    /**
     * Loads reminders based on a custom query.
     *
     * @param query The custom query to execute.
     * @return A list of reminders that match the query.
     */
    public List<Reminder> loadRemindersWithSelection(SupportSQLiteQuery query) {
        return reminderDao.loadRemindersWithSelection(query);
    }
    public interface ReminderInsertCallback {
        void onReminderInserted(long reminderId);
        void onInsertFailed();
    }
}
