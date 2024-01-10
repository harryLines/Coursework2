package com.example.trailblazer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class TripAdapter extends ArrayAdapter<Trip> {
    private final int resource;

    /**
     * Constructor for TripAdapter.
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when instantiating views.
     * @param trips The list of Trip objects to represent in the ListView.
     */
    public TripAdapter(Context context, int resource, List<Trip> trips) {
        super(context, resource, trips);
        this.resource = resource;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(resource, null);
        }

        Trip trip = getItem(position);

        if (trip != null) {
            // Populate the views with data from the Trip object
            TextView dateTextView = view.findViewById(R.id.textViewDate);
            TextView distanceTextView = view.findViewById(R.id.textViewDistance);
            TextView movementTypeTextView = view.findViewById(R.id.textViewMovementType);
            ImageView tripImage = view.findViewById(R.id.imageView);
            ImageView weatherIcon = view.findViewById(R.id.imageViewWeatherIcon);

            // Format the date as "dayOfMonth Month [Year]" or "dayOfMonth Month" (if it's the current year)
            SimpleDateFormat outputFormatWithYear = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
            SimpleDateFormat outputFormatWithoutYear = new SimpleDateFormat("d MMMM", Locale.getDefault());

            String formattedDate;
            if (isCurrentYear(trip.getDate())) {
                formattedDate = outputFormatWithoutYear.format(trip.getDate());
            } else {
                formattedDate = outputFormatWithYear.format(trip.getDate());
            }

            dateTextView.setText(formattedDate);
            distanceTextView.setText(String.format(Locale.UK, "%.2f meters", trip.getDistance()));
            movementTypeTextView.setText(getMovementTypeString(trip.getMovementType()));

            int weatherIconResourceId = getWeatherIconResourceId(trip.getWeather());
            if (weatherIconResourceId != -1) {
                weatherIcon.setImageResource(weatherIconResourceId);
                weatherIcon.setVisibility(View.VISIBLE);
            } else {
                weatherIcon.setVisibility(View.GONE);
            }

            String imagePath = trip.getImage(); // Assuming this returns the file path of the image

            if (imagePath != null) {
                try {
                    File imageFile = new File(imagePath);
                    FileInputStream fis = new FileInputStream(imageFile);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    byte[] imageBytes = bos.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    tripImage.setImageBitmap(bitmap);

                    fis.close();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle the IOException
                }
            } else {
                tripImage.setVisibility(View.GONE);
                // Handle the case where imagePath is null (no image available)
            }
        }
        return view;
    }

    /**
     * Returns the resource ID of the weather icon corresponding to the specified weather type.
     *
     * @param weatherType The weather type for which the icon is needed.
     * @return The resource ID of the corresponding weather icon drawable, or -1 if the weather type is unknown or default.
     */
    private int getWeatherIconResourceId(int weatherType) {
        switch (weatherType) {
            case Trip.WEATHER_SUNNY:
                return R.drawable.sunny_icon;
            case Trip.WEATHER_RAINY:
                return R.drawable.rainy_icon;
            case Trip.WEATHER_SNOW:
                return R.drawable.snowy_icon;
            case Trip.WEATHER_THUNDERSTORM:
                return R.drawable.thunderstorm_icon;
            case Trip.WEATHER_FOGGY:
                return R.drawable.foggy_icon;
            case Trip.WEATHER_WINDY:
                return R.drawable.windy_icon;
            default:
                return -1; // Default or unknown weather type
        }
    }

    /**
     * Determines if the given date is within the current year.
     *
     * @param date The date to check.
     * @return true if the date is within the current year, false otherwise.
     */
    private boolean isCurrentYear(Date date) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String tripYear = yearFormat.format(date);
        String currentYear = yearFormat.format(new Date());
        return tripYear.equals(currentYear);
    }

    /**
     * Converts a movement type integer into a readable string.
     *
     * @param movementType The movement type as an integer. Expected values are 0 (Walking), 1 (Running), and 2 (Cycling).
     * @return A string representing the type of movement. Returns "Unknown" for any values outside the expected range.
     */
    private String getMovementTypeString(int movementType) {
        switch (movementType) {
            case 0:
                return "Walking";
            case 1:
                return "Running";
            case 2:
                return "Cycling";
            default:
                return "Unknown";
        }
    }
}
