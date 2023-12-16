package com.example.TrailBlazer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripsFragment extends Fragment {
    private ListView listView;
    private MapView mapView;
    private GoogleMap googleMap;
    private ArrayAdapter<Trip> tripAdapter;
    View view;
    public TripsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.trips_fragment, container, false);

        // Initialize the ListView
        ListView listView = view.findViewById(R.id.listViewTrips);

        // Read trip history from file
        List<Trip> tripList = loadTripHistory();
        Log.d("TRIP LOAD", "Number of trips loaded: " + tripList.size());

        // Reverse the list to display the most recent trip first
        Collections.reverse(tripList);

        // Create the custom adapter
        tripAdapter = new TripAdapter(requireContext(), R.layout.trip_list_item, tripList);

        // Set the adapter to the ListView
        listView.setAdapter(tripAdapter);

        // Set click listener for the ListView items
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            // Get the selected trip using the adapter
            Trip selectedTrip = tripAdapter.getItem(position);

            if (selectedTrip != null) {
                Log.d("ROUTES", String.valueOf(selectedTrip.getDistance()));
                // Toggle the visibility of the ListView and MapFragment
                List<LatLng> loadedRoutePoints = selectedTrip.getRoutePoints();
                toggleListViewAndMap(loadedRoutePoints);
            }
        });


        MapsInitializer.initialize(getContext());

        return view;
    }

    private List<Trip> loadTripHistory() {
        List<Trip> tripHistory = new ArrayList<>();
        Log.d("TRIP LOAD", "BEGIN LOAD");

        try {
            // Initialize your DatabaseManager
            DatabaseManager databaseManager = new DatabaseManager(requireContext());

            // Load trip history from the database
            tripHistory = databaseManager.loadTripHistory();

            Log.d("TRIP LOAD", "Number of trips loaded from the database: " + tripHistory.size());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tripHistory;
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(); // Return the current date in case of parsing error
        }
    }

    private void toggleListViewAndMap(List<LatLng> routePoints) {
        // Find the mapContainer and listView
        FrameLayout mapContainer = requireView().findViewById(R.id.mapContainer);
        ListView listView = requireView().findViewById(R.id.listViewTrips);

        // Find the back button
        Button backButton = requireView().findViewById(R.id.backButton);

        // Toggle visibility
        if (mapContainer.getVisibility() == View.VISIBLE) {
            mapContainer.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE); // Hide back button in list view
        } else {
            mapContainer.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE); // Show back button in map view

            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(null);
            mapView.onResume(); // needed to get the map to display immediately

            mapView.getMapAsync(googleMap -> {
                if (googleMap != null) {
                    drawRoute(googleMap, routePoints);
                }
            });
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> toggleListViewAndMap(Collections.emptyList()));
    }


    private void drawRoute(GoogleMap googleMap, List<LatLng> routePoints) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(7f)
                .color(getResources().getColor(R.color.blue)); // Assuming you have a color resource named "blue"


        googleMap.addPolyline(polylineOptions);

        Log.d("RoutePoints Size", String.valueOf(routePoints.size()));
        Log.d("RoutePoints Contents", routePoints.toString());

        // Move camera to the first point in the route
        if (!routePoints.isEmpty()) {
            LatLng firstPoint = routePoints.get(0);
            Log.d("FirstPOINT", String.valueOf(routePoints.get(0)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 10f)); // Adjust zoom level here
        }

    }

}

