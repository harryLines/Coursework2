package com.example.trailblazer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TripDao {
    @Query("SELECT * FROM trip_history")
    List<Trip> loadTripHistory();
    @Insert
    void insertTrip(Trip trip);
    @Query("DELETE FROM trip_history")
    void deleteTripHistory();
}
