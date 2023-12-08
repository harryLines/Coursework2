package com.example.coursework2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// SavedLocationsAdapter.java
public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {

    private List<SavedLocation> savedLocations;

    public SavedLocationsAdapter(List<SavedLocation> savedLocations) {
        this.savedLocations = savedLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation location = savedLocations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return savedLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewLocationName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLocationName = itemView.findViewById(R.id.textViewLocationName);
        }

        public void bind(SavedLocation location) {
            textViewLocationName.setText(location.getName());
            // Bind other views if needed
        }
    }
}

