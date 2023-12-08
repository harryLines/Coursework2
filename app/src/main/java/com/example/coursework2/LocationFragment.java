package com.example.coursework2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationFragment extends Fragment {

    private AutoCompleteTextView autoCompleteTextView;
    private RecyclerView recyclerViewSavedLocations;
    private SavedLocationsAdapter savedLocationsAdapter;
    private List<SavedLocation> savedLocations;
    private PlacesClient placesClient;
    private Button btnSaveLocation;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        recyclerViewSavedLocations = view.findViewById(R.id.savedLocationsRecyclerView);

        // Initialize RecyclerView and adapter
        savedLocations = loadSavedLocations(); // Implement this method to load data from the text file
        savedLocationsAdapter = new SavedLocationsAdapter(savedLocations);
        recyclerViewSavedLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSavedLocations.setAdapter(savedLocationsAdapter);

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);

        // Initialize the Places API
        Places.initialize(requireContext(), getString(R.string.google_maps_api_key));
        placesClient = Places.createClient(requireContext());

        // Set up adapter for location suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        // Set up listener for text changes to get location suggestions
        autoCompleteTextView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Fetch location suggestions based on the current text in autoCompleteTextView
                getPlaceSuggestions(s.toString());
            }
        });
        btnSaveLocation = view.findViewById(R.id.btnSaveLocation);
        btnSaveLocation.setOnClickListener(v -> {
            showSaveLocationDialog();
        });

        return view;
    }

    private void showSaveLocationDialog() {
        // Create and customize a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Save Location");

        // Set up the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_location, null);
        builder.setView(dialogView);

        EditText etLocationName = dialogView.findViewById(R.id.etLocationName);

        // Set up the positive button (Save)
        builder.setPositiveButton("Save", (dialog, which) -> {
            String locationName = etLocationName.getText().toString().trim();
            if (!locationName.isEmpty()) {
                // Get the selected location from the AutoCompleteTextView
                String selectedLocation = autoCompleteTextView.getText().toString();

                // Use the selected location to get LatLng using the Places API
                getLatLngForLocation(selectedLocation, locationName);
            } else {
                Toast.makeText(requireContext(), "Please enter a location name", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }

    private void getLatLngForLocation(String placeName, String locationName) {
        // Use the Places API to get details for the selected place
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(placeName)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            if (!response.getAutocompletePredictions().isEmpty()) {
                AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                String placeId = prediction.getPlaceId();

                // Use the Place Details API to get more information about the selected place
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    LatLng selectedLatLng = place.getLatLng();

                    // Create a new SavedLocation instance
                    SavedLocation newLocation = new SavedLocation(locationName, selectedLatLng);

                    // Save the location to the file
                    saveLocationToFile(newLocation);

                    // Update the RecyclerView with the new data
                    savedLocations.add(newLocation);
                    savedLocationsAdapter.notifyDataSetChanged();
                }).addOnFailureListener(exception -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Status status = apiException.getStatus();
                        Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Status status = apiException.getStatus();
                Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getPlaceSuggestions(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a FindAutocompletePredictionsRequest
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build();

        // Use the Places API to get location predictions
        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getFullText(null).toString());
            }

            // Update the adapter with the suggestions
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) autoCompleteTextView.getAdapter();
            adapter.clear();
            adapter.addAll(suggestions);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Status status = apiException.getStatus();
                Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implement SimpleTextWatcher as a helper class to simplify text change handling
    abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private List<SavedLocation> loadSavedLocations() {
        List<SavedLocation> savedLocations = new ArrayList<>();

        try {
            File file = new File(getContext().getFilesDir(), "saved_locations.txt");

            if (!file.exists()) {
                // If the file doesn't exist, create an empty one
                file.createNewFile();
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Split the line using the delimiter
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    double latitude = Double.parseDouble(parts[1].trim());
                    double longitude = Double.parseDouble(parts[2].trim());

                    LatLng latLng = new LatLng(latitude, longitude);
                    SavedLocation savedLocation = new SavedLocation(name, latLng);
                    savedLocations.add(savedLocation);
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedLocations;
    }

    private void saveLocationToFile(SavedLocation newLocation) {
        try {
            File file = new File(getContext().getFilesDir(), "saved_locations.txt");

            if (!file.exists()) {
                // If the file doesn't exist, create a new one
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            // Format the location data and write it to the file
            String locationString = String.format(Locale.UK, "%s,%.6f,%.6f",
                    newLocation.getName(), newLocation.getLatLng().latitude, newLocation.getLatLng().longitude);

            // Write the new location data to the file
            bufferedWriter.write(locationString);
            bufferedWriter.newLine();

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
