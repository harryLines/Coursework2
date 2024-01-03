package com.example.trailblazer;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripsFragment extends Fragment{
    private MapView mapView;
    private ArrayAdapter<Trip> tripAdapter;
    View view;
    private DatabaseManager dbManager;
    public TripsFragment(DatabaseManager dbManager) {
        this.dbManager = dbManager;
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
                toggleListViewAndMap(selectedTrip);
            }
        });

        MapsInitializer.initialize(getContext());

        return view;
    }

    private List<Trip> loadTripHistory() {
        List<Trip> tripHistory = new ArrayList<>();
        Log.d("TRIP LOAD", "BEGIN LOAD");

        try {
            // Load trip history from the database
            tripHistory = dbManager.loadTripHistory();

            Log.d("TRIP LOAD", "Number of trips loaded from the database: " + tripHistory.size());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tripHistory;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the statistics based on the previously selected timeframe
        loadTripHistory();

        List<Trip> reversedTripList = new ArrayList<>(loadTripHistory());
        Collections.reverse(reversedTripList);

        tripAdapter.clear();
        tripAdapter.addAll(reversedTripList);
        tripAdapter.notifyDataSetChanged();
    }

    private void toggleListViewAndMap(Trip trip) {
        // Find the mapContainer and listView
        FrameLayout mapContainer = requireView().findViewById(R.id.mapContainer);
        ListView listView = requireView().findViewById(R.id.listViewTrips);

        // Find the back button
        Button backButton = requireView().findViewById(R.id.backButton);
        LinearLayout lineChartLayout = requireView().findViewById(R.id.lineChartLayout);

        // Toggle visibility
        if (mapContainer.getVisibility() == View.VISIBLE) {
            mapContainer.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE); // Hide back button in list view
            lineChartLayout.setVisibility(View.GONE);
        } else {
            mapContainer.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE); // Show back button in map view
            lineChartLayout.setVisibility(View.VISIBLE);
            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(null);
            mapView.onResume(); // needed to get the map to display immediately

            Log.d("TRIP DATA", String.valueOf(trip.getRoutePoints()));

            List<Double> elevationData = trip.getElevationData();

            List<SavedLocation> savedLocations = dbManager.loadSavedLocations();

            mapView.getMapAsync(googleMap -> {
                for (SavedLocation savedLocation : savedLocations) {
                    LatLng locationLatLng = savedLocation.getLatLng();
                    googleMap.addMarker(new MarkerOptions().position(locationLatLng).title(savedLocation.getName()));
                }

                // Set marker click listener
                googleMap.setOnMarkerClickListener(marker -> {
                    String locationName = marker.getTitle();
                    showRemindersForLocation(locationName); // Implement this method to show reminders for the selected location
                    return true; // Consume the event to prevent the default behavior (opening the info window)
                });

                drawRoute(googleMap, trip.getRoutePoints());
            });

            LineChart lineChart = view.findViewById(R.id.lineChart);

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < elevationData.size(); i++) {
                float durationInMinutes = (i * 30) / 60.0f; // Use 30-second intervals
                entries.add(new Entry(durationInMinutes, elevationData.get(i).floatValue()));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Elevation (m)");
            dataSet.setColor(ThemeManager.getAccentColor(getContext()));
            dataSet.setValueTextColor(Color.WHITE);
            LineData lineData = new LineData(dataSet);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new XAxisValueFormatter()); // Set a custom formatter

            YAxis leftAxis = lineChart.getAxisLeft();
            YAxis rightAxis = lineChart.getAxisRight();
            leftAxis.setDrawGridLines(false);
            rightAxis.setDrawGridLines(false);

            lineChart.setData(lineData);
            lineChart.invalidate();
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> toggleListViewAndMap(null));
    }

    private static class XAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format(Locale.getDefault(), "%.1f min", value);
        }
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

    private void showRemindersForLocation(String locationName) {
        // Get reminders for the specified location name from the database
        List<String> reminders = dbManager.loadRemindersForLocationName(locationName);

        // Create a StringBuilder to build the reminder message
        StringBuilder reminderMessage = new StringBuilder();
        reminderMessage.append("Reminders for ").append(locationName).append(":\n");

        // Append each reminder to the message
        for (String reminder : reminders) {
            reminderMessage.append("- ").append(reminder).append("\n");
        }

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reminders");
        builder.setMessage(reminderMessage.toString());
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Handle OK button click if needed
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

