package com.example.trailblazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * SettingsFragment is a Fragment subclass used to display and manage settings options.
 * It provides an interface for theme changes and data clearing within the app.
 * <p>
 * This fragment is designed to be used within a SettingsActivity context.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        Button btnTheme = view.findViewById(R.id.btnTheme);
        Button btnClearData = view.findViewById(R.id.btnClearData);

        btnTheme.setOnClickListener(v -> {
            if (getActivity() instanceof SettingsActivity) {
                ((SettingsActivity) getActivity()).loadFragment(new ThemeSettingsFragment());
            }
        });

        btnClearData.setOnClickListener(v -> {
            if (getActivity() instanceof SettingsActivity) {
                ((SettingsActivity) getActivity()).loadFragment(new ClearDataFragment());
            }
        });
        return view;
    }
}
