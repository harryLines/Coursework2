package com.example.trailblazer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripsFragment extends Fragment{
    private MapView mapView;
    private ArrayAdapter<Trip> tripAdapter;
    View view;
    private Database database;
    boolean showMap;
    List<Trip> tripHistory;
    ListView listView;
    private Context fragmentContext;
    public TripsFragment() {
        showMap = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.trips_fragment, container, false);

        // Initialize the ListView
        listView = view.findViewById(R.id.listViewTrips);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentContext = requireContext();

        // Now the context is guaranteed to be available
        database = DatabaseManager.getInstance(requireContext());
        MapsInitializer.initialize(requireContext());

        loadTripHistory();
    }

    private void loadTripHistory() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            tripHistory = database.tripDao().loadTripHistory();
            handler.post(() -> {
                Collections.reverse(tripHistory);
                // Initialize the adapter here with tripHistory
                tripAdapter = new TripAdapter(fragmentContext, R.layout.trip_list_item, tripHistory);
                // Set the adapter to the ListView
                listView.setAdapter(tripAdapter);

                // Set click listener for the ListView items
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
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // Update the statistics based on the previously selected timeframe
        loadTripHistory();
        List<Trip> reversedTripList = new ArrayList<>(tripHistory);
        Collections.reverse(reversedTripList);
    }

    private void toggleListViewAndMapLandscape(Trip trip) {
            // Toggle visibility
            mapView = view.findViewById(R.id.mapView);
            mapView.onCreate(null);
            mapView.onResume(); // needed to get the map to display immediately

            List<Double> elevationData = trip.getElevationData();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<SavedLocation> savedLocations = database.savedLocationDao().loadSavedLocations();
            handler.post(() -> {
                if(trip.getRoutePoints() != null) {
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
        });
    }

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

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                List<SavedLocation> savedLocations = database.savedLocationDao().loadSavedLocations();
                handler.post(() -> {
                    if(trip.getRoutePoints() != null) {
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
            });
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
                .color(ContextCompat.getColor(requireContext(), R.color.blue)); // Assuming you have a color resource named "blue"

        googleMap.addPolyline(polylineOptions);

        // Move camera to the first point in the route
        if (!routePoints.isEmpty()) {
            LatLng firstPoint = routePoints.get(0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 10f)); // Adjust zoom level here
        }
    }

    private void showRemindersForLocation(String locationName) {
        // Get reminders for the specified location name from the database

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<Reminder> reminders = database.reminderDao().loadRemindersForLocationName(locationName);
            handler.post(() -> {
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
        });
    }
}

