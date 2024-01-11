package com.example.trailblazer.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.trailblazer.R;
import com.example.trailblazer.data.ReminderRepository;
import com.example.trailblazer.data.SavedLocationRepository;
import com.example.trailblazer.data.TripRepository;


/**
 * A Fragment used to display and handle the clearing of various data types from the database.
 * This includes clearing trip history, reminders, and saved locations.
 */
public class ClearDataFragment extends Fragment {
    Button clearTripHistory;
    Button clearReminders;
    Button clearSavedLocations;
    TripRepository tripRepository;
    SavedLocationRepository savedLocationRepository;
    ReminderRepository reminderRepository;

    /**
     * Default constructor for the ClearDataFragment.
     */
    public ClearDataFragment() {
    }

    /**
     * Called to create the view hierarchy associated with the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any
     *                           views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI
     *                           should be attached to. The fragment should not add the view
     *                           itself, but this can be used to generate the LayoutParams of
     *                           the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.cleardata_fragment, container, false);

        clearTripHistory = view.findViewById(R.id.btnClearTripHistory);
        clearTripHistory.setOnClickListener(v -> {
            tripRepository.deleteTripHistory();
        });

        clearReminders = view.findViewById(R.id.btnClearReminders);
        clearReminders.setOnClickListener(v -> {
            reminderRepository.deleteReminders();
        });

        clearSavedLocations = view.findViewById(R.id.btnClearLocations);
        clearSavedLocations.setOnClickListener(v -> {
            savedLocationRepository.deleteSavedLocations();
        });
        return view;
    }
}
