package com.example.trailblazer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.R;
import com.example.trailblazer.data.Database;
import com.example.trailblazer.data.DatabaseManager;
import com.example.trailblazer.data.Reminder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemindersEditAdapter extends RecyclerView.Adapter<RemindersEditAdapter.ReminderViewHolder> {
    private List<Reminder> reminders;

    /**
     * Constructs a new RemindersEditAdapter with the provided list of reminders.
     *
     * @param reminders The list of reminders to display and edit in the RecyclerView.
     */
    public RemindersEditAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    /**
     * Sets a new list of reminders and updates the RecyclerView.
     *
     * @param reminders The new list of reminders to display and edit.
     */
    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }

    /**
     * Sets the location ID for reminders.
     *
     * @param id The location ID to associate with reminders.
     */
    public void setLocationID(long id) {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_list_item_layout, parent, false);
        return new ReminderViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        String reminder = reminders.get(position).getReminderText();
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        if(reminders != null) {
            return reminders.size();
        }
        return 0;
    }

    /**
     * Removes a reminder item at the specified position.
     *
     * @param position The position of the reminder item to remove.
     * @return The removed Reminder object.
     */
    public Reminder removeItem(int position) {
        // Remove the item from the list
        Reminder reminder = reminders.get(position);
        reminders.remove(position);
        // Notify the adapter that the data set has changed
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        return reminder;
    }

    /**
     * ViewHolder class responsible for holding references to the reminder item views for editing.
     */
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewReminder;

        /**
         * Constructs a new ReminderViewHolder with the provided itemView and adapter.
         *
         * @param itemView The view representing the reminder item.
         * @param adapter  The RemindersEditAdapter to handle item removal.
         */
        public ReminderViewHolder(@NonNull View itemView, RemindersEditAdapter adapter) {
            super(itemView);
            textViewReminder = itemView.findViewById(R.id.textViewReminder);
            ImageButton imageButtonRemove = itemView.findViewById(R.id.imageButtonRemove);

            imageButtonRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Remove the item from the list and the database
                    Reminder reminder = adapter.removeItem(position);
                    Database database = DatabaseManager.getInstance(itemView.getContext());

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> database.reminderDao().removeReminder(reminder.getId()));
                }
            });
        }

        /**
         * Binds the given reminder data to the views within the ViewHolder.
         *
         * @param reminder The reminder text to bind to the view.
         */
        public void bind(String reminder) {
            textViewReminder.setText(reminder);
        }
    }
}
