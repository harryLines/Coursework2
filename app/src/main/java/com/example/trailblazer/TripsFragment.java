package com.example.trailblazer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripsFragment extends Fragment{
    private MapView mapView;
    private ArrayAdapter<Trip> tripAdapter;
    View view;
    boolean showMap;
    List<Trip> tripHistory;
    ListView listView;
    private Context fragmentContext;
    private TripRepository tripRepository;
    private SavedLocationRepository savedLocationRepository;
    ReminderRepository reminderRepository;
    public TripsFragment() {
        showMap = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.trips_fragment, container, false);
        listView = view.findViewById(R.id.listViewTrips);
        tripRepository = new TripRepository(DatabaseManager.getInstance(requireContext()).tripDao());
        savedLocationRepository = new SavedLocationRepository(DatabaseManager.getInstance(requireContext()).savedLocationDao());
        reminderRepository = new ReminderRepository(DatabaseManager.getInstance(requireContext()).reminderDao());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentContext = requireContext();

        MapsInitializer.initialize(requireContext());

        loadTripHistory();
    }

    /**
     * Loads trip history data from the database and initializes the ListView adapter.
     */
    private void loadTripHistory() {
        tripRepository.loadTripHistory().observe(getViewLifecycleOwner(), trips -> {
            Collections.reverse(trips); // Reverse the list if necessary
            tripHistory = trips;
            // Initialize the adapter here with tripHistory
            tripAdapter = new TripAdapter(fragmentContext, R.layout.trip_list_item, tripHistory);
            // Set the adapter to the ListView
            listView.setAdapter(tripAdapter);
            // Set click listener for th ListView items
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                // Get the selected trip using the adapter
                showMap = !showMap;
                Trip selectedTrip = tripAdapter.getItem(position);
                if (selectedTrip != null) {
                    // Toggle the visibility of the ListView and MapFragment
                    int orientation = getResources().getConfiguration().orientation;
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        toggleListViewAndMapPortrait(selectedTrip);
                    } else {
                        toggleListViewAndMapLandscape(selectedTrip);
                    }
                }
            });
        });
    }


    /**
     * Called when the fragment is visible to the user and actively running. Reloads the tripList.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Update the statistics based on the previously selected timeframe
        loadTripHistory();
        if(tripHistory != null) {
            List<Trip> reversedTripList = new ArrayList<>(tripHistory);
            Collections.reverse(reversedTripList);
        }
    }

    /**
     * Toggles the ListView and MapView visibility in landscape mode and displays trip details.
     *
     * @param trip The selected Trip object to display.
     */
    private void toggleListViewAndMapLandscape(Trip trip) {
            // Toggle visibility
            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(null);
            mapView.onResume(); // needed to get the map to display immediately

        loadSavedLocationsAndMap(trip);
    }

    /**
     * Toggles the ListView and MapView visibility in portrait mode and displays trip details.
     *
     * @param trip The selected Trip object to display.
     */
    private void toggleListViewAndMapPortrait(Trip trip) {
        // Find the mapContainer and listView
        FrameLayout mapContainer = requireView().findViewById(R.id.mapContainer);
        ListView listView = requireView().findViewById(R.id.listViewTrips);
        // Find the back button
        Button backButton = requireView().findViewById(R.id.backButton);
        LinearLayout lineChartLayout = requireView().findViewById(R.id.lineChartLayout);
        // Toggle visibility
        if (showMap) {
            mapContainer.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE); // Hide back button in list view
            lineChartLayout.setVisibility(View.VISIBLE);
            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(null);
            mapView.onResume();

            List<Double> elevationData = trip.getElevationData();

            loadSavedLocationsAndMap(trip);

            if(elevationData != null) {
                LineChart lineChart = view.findViewById(R.id.lineChart);

                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < elevationData.size(); i++) {
                    float durationInMinutes = (i * 30) / 60.0f; // Use 30-second intervals
                    entries.add(new Entry(durationInMinutes, elevationData.get(i).floatValue()));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Elevation (m)");
                dataSet.setColor(ThemeManager.getAccentColor(requireContext()));
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
        } else {
            mapContainer.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.GONE);
            lineChartLayout.setVisibility(View.GONE);// needed to get the map to display immediately
        }

        // Set click listener for the back button
        backButton.setOnClickListener(v -> {
            showMap = !showMap;
            toggleListViewAndMapPortrait(null);
        });
    }

    /**
     * Observes and loads saved locations from the repository, and displays them on a map along with the route of the given trip.
     * Each saved location is marked on the map with a marker. When a marker is clicked, it shows reminders associated with that location.
     * The method also draws the route of the trip on the map if route points are available.
     *
     * @param trip The trip whose route points are to be drawn on the map. This trip contains the information needed to plot the route.
     */
    private void loadSavedLocationsAndMap(Trip trip) {
        savedLocationRepository.loadSavedLocations().observe(getViewLifecycleOwner(), savedLocations -> {
            if (trip.getRoutePoints() != null) {
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
            }
        });
    }

    /**
     * Custom formatter for the XAxis values of the chart.
     */
    private static class XAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format(Locale.getDefault(), "%.1f min", value);
        }
    }

    /**
     * Draws a route on the GoogleMap using a list of LatLng points.
     *
     * @param googleMap The GoogleMap object on which the route will be drawn.
     * @param routePoints The list of LatLng points representing the route.
     */
    private void drawRoute(GoogleMap googleMap, List<LatLng> routePoints) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(7f)
                .color(ContextCompat.getColor(requireContext(), R.color.blue)); // Assuming you have a color resource named "blue"

        googleMap.addPolyline(polylineOptions);

        // Move camera to the first point in the route
        if (!routePoints.isEmpty()) {
            LatLng firstPoint = routePoints.get(0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 10f)); // Adjust zoom level here
        }
    }

    /**
     * Shows a list of reminders for a given location in an AlertDialog.
     *
     * @param locationName The name of the location for which reminders are to be shown.
     */
    private void showRemindersForLocation(String locationName) {
        // Assuming reminderRepository is already initialized
        // Observe LiveData from the repository
        reminderRepository.loadRemindersForLocationName(locationName).observe(getViewLifecycleOwner(), reminders -> {
            StringBuilder reminderMessage = new StringBuilder();
            reminderMessage.append("Reminders for ").append(locationName).append(":\n");

            // Append each reminder to the message
            for (Reminder reminder : reminders) {
                reminderMessage.append("- ").append(reminder.getReminderText()).append("\n");
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
        });
    }
}

