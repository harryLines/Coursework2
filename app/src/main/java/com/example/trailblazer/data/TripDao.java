package com.example.trailblazer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TripDao {
    @Query("SELECT * FROM trip_history")
    LiveData<List<Trip>> loadTripHistory();
    @Insert
    long addNewTrip(Trip trip);
    @Query("DELETE FROM trip_history")
    void deleteTripHistory();
}
