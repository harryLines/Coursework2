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

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class TripAdapter extends ArrayAdapter<Trip> {
    private static class ViewHolder {
        TextView dateTextView;
        TextView distanceTextView;
        TextView movementTypeTextView;
        ImageView tripImage;
        ImageView weatherIcon;
    }
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

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.dateTextView = convertView.findViewById(R.id.textViewDate);
            holder.distanceTextView = convertView.findViewById(R.id.textViewDistance);
            holder.movementTypeTextView = convertView.findViewById(R.id.textViewMovementType);
            holder.tripImage = convertView.findViewById(R.id.imageView);
            holder.weatherIcon = convertView.findViewById(R.id.imageViewWeatherIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Trip trip = getItem(position);
        if (trip != null) {
            SimpleDateFormat outputFormatWithYear = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
            SimpleDateFormat outputFormatWithoutYear = new SimpleDateFormat("d MMMM", Locale.getDefault());
            String formattedDate = isCurrentYear(trip.getDate()) ? outputFormatWithoutYear.format(trip.getDate()) : outputFormatWithYear.format(trip.getDate());

            holder.dateTextView.setText(formattedDate);
            holder.distanceTextView.setText(String.format(Locale.UK, "%.2f meters", trip.getDistance()));
            holder.movementTypeTextView.setText(getMovementTypeString(trip.getMovementType()));
            holder.weatherIcon.setImageResource(getWeatherIconResourceId(trip.getWeather()));
            holder.weatherIcon.setVisibility(getWeatherIconResourceId(trip.getWeather()) != -1 ? View.VISIBLE : View.GONE);

            String imagePath = trip.getImage();
            if (imagePath != null) {
                Glide.with(getContext()).load(new File(imagePath)).into(holder.tripImage);
                holder.tripImage.setVisibility(View.VISIBLE);
            } else {
                holder.tripImage.setVisibility(View.GONE);
            }
        }

        return convertView;
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
