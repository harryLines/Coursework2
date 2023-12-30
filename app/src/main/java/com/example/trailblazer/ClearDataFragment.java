package com.example.trailblazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class ClearDataFragment extends Fragment {
    Button clearTripHistory;
    private DatabaseManager dbManager;
    public ClearDataFragment(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.cleardata_fragment, container, false);

        clearTripHistory = view.findViewById(R.id.btnClearTripHistory);

        clearTripHistory.setOnClickListener(v -> {
            // Use the injected DatabaseManager
            if (dbManager != null) {
                dbManager.deleteTripHistory();
            }
        });

        return view;

    }
}
