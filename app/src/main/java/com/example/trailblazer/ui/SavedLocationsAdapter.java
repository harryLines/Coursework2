package com.example.trailblazer.ui;

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
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.R;
import com.example.trailblazer.data.DatabaseManager;
import com.example.trailblazer.data.Reminder;
import com.example.trailblazer.data.ReminderRepository;
import com.example.trailblazer.data.SavedLocation;
import com.example.trailblazer.data.SavedLocationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder> {
    private final List<SavedLocation> savedLocations;
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private SavedLocationRepository savedLocationRepository;
    private ReminderRepository reminderRepository;

    /**
     * Constructs a SavedLocationsAdapter with the given list of saved locations and context.
     *
     * @param savedLocations The list of saved locations to be displayed.
     * @param context        The context associated with the adapter.
     */
    public SavedLocationsAdapter(List<SavedLocation> savedLocations, Context context, LifecycleOwner lifecycleOwner) {
        this.savedLocations = savedLocations;
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        savedLocationRepository = new SavedLocationRepository(DatabaseManager.getInstance(context).savedLocationDao());
        reminderRepository = new ReminderRepository(DatabaseManager.getInstance(context).reminderDao());
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     *
     * @param parent   The parent view group.
     * @param viewType The view type of the new ViewHolder.
     * @return A new ViewHolder that holds a view for the saved location item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_location, parent, false);
        savedLocationRepository = new SavedLocationRepository(DatabaseManager.getInstance(context).savedLocationDao());
        reminderRepository = new ReminderRepository(DatabaseManager.getInstance(context).reminderDao());
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at a specified position.
     *
     * @param holder   The ViewHolder that should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation location = savedLocations.get(position);
        holder.bind(location);
        holder.itemView.setOnClickListener(v -> {
            // Show reminders dialog when an item is clicked
            showRemindersDialog(savedLocations.get(position));
        });
    }

    /**
     * Displays a dialog for editing reminders associated with a saved location.
     *
     * @param savedLocation The SavedLocation object for which reminders are to be edited.
     */
    private void showRemindersDialog(SavedLocation savedLocation) {
        // Create and customize a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reminders for " + savedLocation.getName() + " ID: " + savedLocation.getLocationID());

        // Set up the layout for the dialog
        View dialogView = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_edit_reminders, null);
        builder.setView(dialogView);

        // Initialize views
        RecyclerView recyclerViewReminders = dialogView.findViewById(R.id.recyclerViewReminders);
        EditText editTextNewReminder = dialogView.findViewById(R.id.editTextNewReminder);
        Button btnAddReminder = dialogView.findViewById(R.id.btnAddReminder);

        setUpRemindersAdapter(savedLocation,recyclerViewReminders);

        // Set up the 'Add Reminder' button click listener
        setUpAddReminderButton(savedLocation, editTextNewReminder, btnAddReminder);

        // Set up the positive button (Close)
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    /**
     * Sets up and returns a RemindersEditAdapter for displaying reminders associated with a saved location.
     *
     * @param savedLocation The SavedLocation object for which reminders are to be displayed.
     * @param recyclerView   The RecyclerView used to display reminders.
     * @return The configured RemindersEditAdapter.
     */
    private void setUpRemindersAdapter(SavedLocation savedLocation, RecyclerView recyclerView) {
        RemindersEditAdapter remindersAdapter = new RemindersEditAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(remindersAdapter);

        reminderRepository.loadRemindersForLocation(savedLocation.getLocationID()).observe(lifecycleOwner, remindersList -> {
            if (remindersList != null) {
                remindersAdapter.setReminders(remindersList);
                remindersAdapter.setLocationID(savedLocation.getLocationID());
            }
        });
    }

    /**
     * Sets up the 'Add Reminder' button click listener for adding new reminders to a saved location.
     *
     * @param savedLocation   The SavedLocation object to which reminders are added.
     * @param editText        The EditText for entering a new reminder.
     * @param addButton       The Button used to add a new reminder.
     */
    private void setUpAddReminderButton(SavedLocation savedLocation, EditText editText, Button addButton) {
        addButton.setOnClickListener(v -> {
            String newReminderText = editText.getText().toString().trim();
            Reminder newReminder = new Reminder(savedLocation.getLocationID(), newReminderText);

            if (savedLocation.getReminders() != null && !newReminderText.isEmpty() && !savedLocation.getReminders().contains(newReminder)) {
                savedLocation.addReminder(newReminder);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    for (Reminder reminder : savedLocation.getReminders()) {
                        reminderRepository.addNewReminder(reminder,null);
                    }
                });
            }
        });
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
