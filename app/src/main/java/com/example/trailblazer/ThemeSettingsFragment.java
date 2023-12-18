package com.example.trailblazer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

public class ThemeSettingsFragment extends Fragment {

    private Spinner dropDownThemes;

    public ThemeSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.theme_fragment, container, false);

        dropDownThemes = view.findViewById(R.id.dropDownTheme);

        // Get the theme names from resources
        String[] themeNames = getResources().getStringArray(R.array.theme_names);

        // Create an ArrayAdapter using a simple spinner layout and define the themes
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, themeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        dropDownThemes.setAdapter(adapter);

        int currentThemeId = ThemeManager.getSelectedTheme(requireContext());
        int defaultSelection = getThemePosition(themeNames, currentThemeId);
        dropDownThemes.setSelection(defaultSelection);

        // Set a listener to handle item selections
        dropDownThemes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Apply the selected theme
                applySelectedTheme(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        return view;
    }

    private void applySelectedTheme(int position) {
        int[] themeIds = ThemeManager.getAllThemes();
        ThemeManager.applyTheme(requireActivity(), themeIds[position]);
        ThemeManager.saveSelectedTheme(requireContext(), themeIds[position]);
    }

    private int getThemePosition(String[] themeNames, int themeId) {
        for (int i = 0; i < themeNames.length; i++) {
            if (themeId == ThemeManager.getAllThemes()[i]) {
                return i;
            }
        }
        return 0; // Default to the first theme if not found
    }
}
