package com.example.trailblazer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface SavedLocationDao {
    @Insert
    long addNewLocation(SavedLocation location);
    // Delete reminders

    @Query("DELETE FROM saved_locations")
    void deleteSavedLocations();

    @Query("SELECT * FROM saved_locations")
    List<SavedLocation> loadSavedLocations();
}
