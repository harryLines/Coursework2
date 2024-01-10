package com.example.trailblazer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The LocationFragment class manages the user's location-related activities and provides features
 * for searching, saving, and displaying locations.
 */
public class LocationFragment extends Fragment {

    private AutoCompleteTextView autoCompleteTextView;
    private SavedLocationsAdapter savedLocationsAdapter;
    private List<SavedLocation> savedLocations;
    private PlacesClient placesClient;
    private Database database;
    View view;
    public LocationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.location_fragment, container, false);

        database = DatabaseManager.getInstance(requireContext());

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        RecyclerView recyclerViewSavedLocations = view.findViewById(R.id.savedLocationsRecyclerView);

        // Initialize RecyclerView and adapter
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Load saved locations from the database
            savedLocations = database.savedLocationDao().loadSavedLocations();
            savedLocationsAdapter = new SavedLocationsAdapter(savedLocations,getContext());
            recyclerViewSavedLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerViewSavedLocations.setAdapter(savedLocationsAdapter);
            //Background work here
            handler.post(() -> {
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
                Button btnSaveLocation = view.findViewById(R.id.btnSaveLocation);
                btnSaveLocation.setOnClickListener(v -> {
                    showSaveLocationDialog();
                });
            });
        });

        return view;
    }

    /**
     * Show a dialog for saving a new location with a custom name.
     */
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

    /**
     * Retrieves the latitude and longitude coordinates for a selected place using the Places API
     * and saves the location with the provided name.
     *
     * @param placeName     The name of the selected place.
     * @param locationName  The custom name for the saved location.
     */
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
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    LatLng selectedLatLng = place.getLatLng();

                    // Create a new SavedLocation instance
                    assert selectedLatLng != null;
                    // Create a new SavedLocation instance with the locationId
                    SavedLocation newLocation = new SavedLocation(locationName, selectedLatLng);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        long locationId = database.savedLocationDao().addNewLocation(newLocation);
                        // Update the savedLocations list with the new data
                        newLocation.setLocationID(locationId);
                        savedLocations.add(newLocation);
                        //Background work here
                        handler.post(() -> {
                            RecyclerView recyclerViewSavedLocations = view.findViewById(R.id.savedLocationsRecyclerView);
                            savedLocationsAdapter.notifyDataSetChanged(); // Notify adapter of dataset change
                        });
                    });
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

    /**
     * Retrieves location suggestions based on user input using the Places API
     * and updates the AutoCompleteTextView's adapter with the suggestions.
     *
     * @param query  The user's input query.
     */
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

    /**
     * A helper class that simplifies text change handling for the AutoCompleteTextView.
     */
    abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
