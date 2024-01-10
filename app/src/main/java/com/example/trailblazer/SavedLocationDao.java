package com.example.trailblazer;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface SavedLocationDao {
    @Insert
    long addNewLocation(SavedLocation location);
    @Query("DELETE FROM saved_locations")
    void deleteSavedLocations();
    @Query("SELECT * FROM saved_locations")
    LiveData<List<SavedLocation>> loadSavedLocations();
}
