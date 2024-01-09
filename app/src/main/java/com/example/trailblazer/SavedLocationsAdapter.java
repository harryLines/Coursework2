package com.example.trailblazer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {

    private final List<SavedLocation> savedLocations;
    private final Context context;
    private Database database;
    RemindersEditAdapter remindersAdapter;

    public SavedLocationsAdapter(List<SavedLocation> savedLocations, Context context) {
        this.savedLocations = savedLocations;
        this.context = context;
        database = DatabaseManager.getInstance(context);
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
        builder.setTitle("Reminders for " + savedLocation.getName() + "ID: " + savedLocation.getLocationID());

        // Set up the layout for the dialog
        View dialogView = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_edit_reminders, null);
        builder.setView(dialogView);

        RecyclerView recyclerViewReminders = dialogView.findViewById(R.id.recyclerViewReminders);
        EditText editTextNewReminder = dialogView.findViewById(R.id.editTextNewReminder);
        Button btnAddReminder = dialogView.findViewById(R.id.btnAddReminder);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Reminder> remindersList = database.reminderDao().loadRemindersForLocation(savedLocation.getLocationID());
            RemindersEditAdapter remindersAdapter = new RemindersEditAdapter(remindersList);
            remindersAdapter.setReminders(remindersList);
            remindersAdapter.setLocationID(savedLocation.getLocationID());
            //Background work here
            handler.post(() -> {
                recyclerViewReminders.setLayoutManager(new LinearLayoutManager(context));
                recyclerViewReminders.setAdapter(remindersAdapter);

                btnAddReminder.setOnClickListener(v -> {
                    String newReminderText = editTextNewReminder.getText().toString().trim();
                    Reminder newReminder = new Reminder(savedLocation.getLocationID(), newReminderText);
                    if(savedLocation.getReminders() != null) {
                        if (!newReminderText.isEmpty() && !savedLocation.getReminders().contains(newReminder)) {
                            savedLocation.addReminder(newReminder);

                            ExecutorService executorBtn = Executors.newSingleThreadExecutor();
                            Handler handlerBtn = new Handler(Looper.getMainLooper());

                            executorBtn.execute(() -> {
                                for (Reminder reminder : savedLocation.getReminders()) {
                                    database.reminderDao().addNewReminder(reminder);
                                }
                                //Background work here
                                handlerBtn.post(() -> {
                                    remindersAdapter.setReminders(savedLocation.getReminders());
                                    remindersAdapter.setLocationID(savedLocation.getLocationID());
                                    editTextNewReminder.setText("");
                                });
                            });
                        }
                    }
                });
            });
        });

        // Set up the positive button (Close)
        builder.setPositiveButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return savedLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewLocationName;

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
