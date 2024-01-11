package com.example.trailblazer.ui;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.trailblazer.data.SavedLocation;
import com.example.trailblazer.data.SavedLocationRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LocationFragmentViewModel extends ViewModel {

    private final SavedLocationRepository savedLocationRepository;
    private final MutableLiveData<List<String>> placeSuggestions = new MutableLiveData<>();
    private final PlacesClient placesClient;

    @Inject
    public LocationFragmentViewModel(SavedLocationRepository savedLocationRepository, PlacesClient placesClient) {
        this.savedLocationRepository = savedLocationRepository;
        this.placesClient = placesClient;
    }

    public LiveData<List<SavedLocation>> getSavedLocations() {
        return savedLocationRepository.loadSavedLocations();
    }

    public LiveData<List<String>> getPlaceSuggestions() {
        return placeSuggestions;
    }

    public void fetchPlaceSuggestions(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getFullText(null).toString());
            }
            placeSuggestions.postValue(suggestions);
        }).addOnFailureListener(exception -> {
            Log.d("FAILURE", String.valueOf(exception));
        });
    }

    public void addNewLocation(String locationName, LatLng latLng) {
        SavedLocation newLocation = new SavedLocation(locationName, latLng);
        savedLocationRepository.addNewLocation(newLocation, null);
    }
}
