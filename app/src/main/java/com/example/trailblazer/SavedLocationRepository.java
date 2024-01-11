package com.example.trailblazer;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for managing SavedLocation entities.
 */

public class SavedLocationRepository {
    private final SavedLocationDao savedLocationDao;
    private final Executor executor;

    /**
     * Constructor for SavedLocationRepository.
     *
     * @param savedLocationDao DAO for accessing saved locations data.
     */

    public SavedLocationRepository(SavedLocationDao savedLocationDao) {
        this.savedLocationDao = savedLocationDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Adds a new location to the database.
     *
     * @param location The location to be added.
     */
    public void addNewLocation(SavedLocation location,SavedLocationInsertCallback callback) {
        executor.execute(() -> savedLocationDao.addNewLocation(location));
    }

    /**
     * Deletes all saved locations from the database.
     */
    public void deleteSavedLocations() {
        executor.execute(savedLocationDao::deleteSavedLocations);
    }

    /**
     * Retrieves all saved locations from the database.
     *
     * @return A list of saved locations.
     */
    public LiveData<List<SavedLocation>> loadSavedLocations() {
        return savedLocationDao.loadSavedLocations();
    }

    public interface SavedLocationInsertCallback {
        void onSavedLocationInserted(long locationId);
        void onInsertFailed();
    }
}
