package com.example.coursework2;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            // Get the selected trip
            Trip selectedTrip = tripAdapter.getItem(position);

            // Toggle the visibility of the ListView and MapFragment
            toggleListViewAndMap(selectedTrip.getRoutePoints());
        });

        MapsInitializer.initialize(getContext());

        return view;
    }

    private List<Trip> loadTripHistory() {
        List<Trip> tripHistory = new ArrayList<>();
        Log.d("TRIP LOAD", "BEGIN LOAD");

        try {
            File file = new File(getContext().getFilesDir(), "trip_history.txt");

            if (!file.exists()) {
                // File doesn't exist, return an empty list
                Log.d("TRIP LOAD", "NO FILE");
                return tripHistory;
            }

            Log.d("TRIP LOAD", "FILE FOUND");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d("LINE LOAD",line);
                // Parse each line to create the Trip object
                String[] parts = line.split(",");
                Log.d("LENGTH", String.valueOf(parts.length));
                if (parts.length == 5) {
                    int movementType = Integer.parseInt(parts[0].trim());
                    Date date = parseDate(parts[1].trim());
                    double distance = Double.parseDouble(parts[2].trim());
                    long time = Long.parseLong(parts[3].trim());

                    // Parse the route points array from the last part
                    String routePointsString = parts[4].trim();
                    List<LatLng> routePoints = parseRoutePoints(routePointsString);

                    Log.d("RoutePoints Contents", routePoints.toString());

                    Trip trip = new Trip(date, distance, movementType, time, routePoints);
                    Log.d("TRIP LOAD", String.valueOf(trip.getDistance()));
                    tripHistory.add(trip);
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tripHistory;
    }

    private List<LatLng> parseRoutePoints(String routePointsString) {
        List<LatLng> routePoints = new ArrayList<>();

        // Remove brackets from the string
        routePointsString = routePointsString.substring(1, routePointsString.length() - 1);

        // Split the string into sets of coordinates
        String[] coordinateSets = routePointsString.split("\\|");

        for (String coordinateSet : coordinateSets) {
            // Skip empty elements
            if (!coordinateSet.trim().isEmpty()) {
                // Remove leading and trailing whitespace
                coordinateSet = coordinateSet.trim();

                // Remove square brackets and parentheses
                coordinateSet = coordinateSet.replace("[", "").replace("]", "").replace("(", "").replace(")", "");

                // Split the set into individual coordinates
                String[] latLng = coordinateSet.split(";");
                if (latLng.length == 2) { // Ensure there are latitude and longitude
                    double latitude = Double.parseDouble(latLng[0].trim());
                    double longitude = Double.parseDouble(latLng[1].trim());
                    routePoints.add(new LatLng(latitude, longitude));
                }
            }
        }

        return routePoints;
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

