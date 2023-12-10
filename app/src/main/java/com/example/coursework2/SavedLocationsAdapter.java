package com.example.coursework2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

// SavedLocationsAdapter.java
public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {

    private List<SavedLocation> savedLocations;
    private Context context;

    public SavedLocationsAdapter(List<SavedLocation> savedLocations, Context context) {
        this.savedLocations = savedLocations;
        this.context = context;
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
        holder.itemView.setOnClickListener(v -> {
            // Show reminders dialog when an item is clicked
            showRemindersDialog(savedLocations.get(position));
        });
    }

    private void showRemindersDialog(SavedLocation savedLocation) {
        // Create and customize a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reminders for " + savedLocation.getName());

        // Set up the layout for the dialog
        View dialogView = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_edit_reminders, null);
        builder.setView(dialogView);

        TextView textViewReminders = dialogView.findViewById(R.id.textViewReminders);
        EditText editTextNewReminder = dialogView.findViewById(R.id.editTextNewReminder);
        Button btnAddReminder = dialogView.findViewById(R.id.btnAddReminder);

        // Update the existing reminders text
        StringBuilder remindersText = new StringBuilder();
        for (String reminder : savedLocation.getReminders()) {
            remindersText.append("- ").append(reminder).append("\n");
        }
        textViewReminders.setText(remindersText.toString());

        // Add a new reminder
        btnAddReminder.setOnClickListener(v -> {
            String newReminder = editTextNewReminder.getText().toString().trim();
            if (!newReminder.isEmpty()) {
                savedLocation.addReminder(newReminder);

                // Save the updated reminders to the file
                saveRemindersToFile(savedLocation);

                // Update the displayed reminders text
                remindersText.append("- ").append(newReminder).append("\n");
                textViewReminders.setText(remindersText.toString());

                // Clear the EditText for the next reminder
                editTextNewReminder.setText("");
            }
        });

        // Set up the positive button (Close)
        builder.setPositiveButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }

    private void saveRemindersToFile(SavedLocation savedLocation) {
        try {
            File file = new File(context.getFilesDir(), "saved_locations.txt");

            if (!file.exists()) {
                // If the file doesn't exist, create a new one
                file.createNewFile();
            }

            // Read existing content from the file
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0].trim();
                double latitude = Double.parseDouble(parts[1].trim());
                double longitude = Double.parseDouble(parts[2].trim());

                // Check if the current line corresponds to the saved location
                if (name.equals(savedLocation.getName())
                        && latitude == savedLocation.getLatLng().latitude
                        && longitude == savedLocation.getLatLng().longitude) {
                    line += "," + savedLocation.getRemindersAsString();
                }

                fileContent.append(line).append("\n");
            }

            bufferedReader.close();

            // Write the updated content back to the file
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(fileContent.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

