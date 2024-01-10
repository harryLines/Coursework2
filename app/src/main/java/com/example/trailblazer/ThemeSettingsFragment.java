package com.example.trailblazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;

/**
 * ThemeSettingsFragment is used for handling the theme settings of the application.
 * It provides an interface for the user to select and apply different themes.
 */
public class ThemeSettingsFragment extends Fragment {
    public ThemeSettingsFragment() {
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.theme_fragment, container, false);

        Spinner dropDownThemes = view.findViewById(R.id.dropDownTheme);

        // Get the theme names from resources and set up the spinner adapter
        String[] themeNames = getResources().getStringArray(R.array.theme_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, themeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDownThemes.setAdapter(adapter);

        // Set the spinner to the current theme
        int currentThemeId = ThemeManager.getSelectedTheme(requireContext());
        int defaultSelection = getThemePosition(themeNames, currentThemeId);
        dropDownThemes.setSelection(defaultSelection);

        // Set a listener to handle item selections
        dropDownThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                applySelectedTheme(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        return view;
    }

    /**
     * Applies the selected theme and saves it as the user's preference.
     *
     * @param position The position of the selected theme in the array.
     */
    private void applySelectedTheme(int position) {
        int[] themeIds = ThemeManager.getAllThemes();
        ThemeManager.applyTheme(requireActivity(), themeIds[position]);
        ThemeManager.saveSelectedTheme(requireContext(), themeIds[position]);
    }

    /**
     * Retrieves the position of the current theme in the array of theme names.
     *
     * @param themeNames Array of theme names.
     * @param themeId The ID of the current theme.
     * @return The position of the theme in the array.
     */
    private int getThemePosition(String[] themeNames, int themeId) {
        for (int i = 0; i < themeNames.length; i++) {
            if (themeId == ThemeManager.getAllThemes()[i]) {
                return i;
            }
        }
        return 0; // Default to the first theme if not found
    }
}
