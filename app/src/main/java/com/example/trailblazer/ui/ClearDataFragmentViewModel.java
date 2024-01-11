package com.example.trailblazer.ui;

import androidx.lifecycle.ViewModel;

import com.example.trailblazer.data.ReminderRepository;
import com.example.trailblazer.data.SavedLocationRepository;
import com.example.trailblazer.data.TripRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ClearDataFragmentViewModel extends ViewModel {
    private final TripRepository tripRepository;
    private final SavedLocationRepository savedLocationRepository;
    private final ReminderRepository reminderRepository;

    @Inject
    public ClearDataFragmentViewModel(
            TripRepository tripRepository,
            SavedLocationRepository savedLocationRepository,
            ReminderRepository reminderRepository) {
        this.tripRepository = tripRepository;
        this.savedLocationRepository = savedLocationRepository;
        this.reminderRepository = reminderRepository;
    }

    // Add methods to perform actions using the repositories

    public void deleteTripHistory() {
        tripRepository.deleteTripHistory();
    }

    public void deleteReminders() {
        reminderRepository.deleteReminders();
    }

    public void deleteSavedLocations() {
        savedLocationRepository.deleteSavedLocations();
    }
}
