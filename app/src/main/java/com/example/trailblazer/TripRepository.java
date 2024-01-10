package com.example.trailblazer;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for managing Trip entities.
 */
public class TripRepository {
    private final TripDao tripDao;
    private final Executor executor;

    /**
     * Constructor for TripRepository.
     *
     * @param tripDao DAO for accessing trip data.
     */
    public TripRepository(TripDao tripDao) {
        this.tripDao = tripDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves all trip history from the database.
     *
     * @return A LiveData list of all trips.
     */
    public LiveData<List<Trip>> loadTripHistory() {
        // Assuming you want to observe changes in the database and update the UI accordingly
        // Change return type to LiveData if you want real-time updates in the UI
        return tripDao.loadTripHistory();
    }

    /**
     * Adds a new trip to the database.
     *
     * @param trip The trip to be added.
     */
    public void addNewTrip(Trip trip, TripRepository.TripInsertCallback callback) {
        executor.execute(() -> tripDao.addNewTrip(trip));
    }

    /**
     * Deletes all trip history from the database.
     */
    public void deleteTripHistory() {
        executor.execute(tripDao::deleteTripHistory);
    }
    public interface TripInsertCallback {
        void onTripInserted(long tripId);
        void onInsertFailed();
    }
}
