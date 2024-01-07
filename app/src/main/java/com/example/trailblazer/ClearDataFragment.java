package com.example.trailblazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class ClearDataFragment extends Fragment {
    Button clearTripHistory;
    Button clearReminders;
    Button clearSavedLocations;
    DatabaseManager dbManager;
    public ClearDataFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.cleardata_fragment, container, false);
        Database database = DatabaseManager.getInstance(requireContext());

        TripDao tripDao = database.tripDao();
        ReminderDao reminderDao = database.reminderDao();
        SavedLocationDao savedLocationDao = database.savedLocationDao();

        clearTripHistory = view.findViewById(R.id.btnClearTripHistory);
        clearTripHistory.setOnClickListener(v -> {
            // Use the injected DatabaseManager
            if (dbManager != null) {
                tripDao.deleteTripHistory();
            }
        });

        clearReminders = view.findViewById(R.id.btnClearReminders);
        clearReminders.setOnClickListener(v -> {
            if (dbManager != null) {
                reminderDao.deleteReminders();
            }
        });

        clearSavedLocations = view.findViewById(R.id.btnClearLocations);
        clearSavedLocations.setOnClickListener(v -> {
            if (dbManager != null) {
                savedLocationDao.deleteSavedLocations();
            }
        });
        return view;
    }
}
