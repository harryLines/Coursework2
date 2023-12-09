package com.example.coursework2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class TripAdapter extends ArrayAdapter<Trip> {
    private int resource;

    public TripAdapter(Context context, int resource, List<Trip> trips) {
        super(context, resource, trips);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        }

        return view;
    }

    private boolean isCurrentYear(Date date) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        String tripYear = yearFormat.format(date);
        String currentYear = yearFormat.format(new Date());
        return tripYear.equals(currentYear);
    }

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
