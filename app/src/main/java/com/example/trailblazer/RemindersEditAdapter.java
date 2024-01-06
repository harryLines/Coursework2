package com.example.trailblazer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RemindersEditAdapter extends RecyclerView.Adapter<RemindersEditAdapter.ReminderViewHolder> {
    private List<String> reminders;
    private long locationID;

    public RemindersEditAdapter(List<String> reminders) {
        this.reminders = reminders;
    }

    public void setReminders(List<String> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }
    public void setLocationID(long id) {
        this.locationID = id;
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
        String reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public String removeItem(int position) {
        // Remove the item from the list
        String text = reminders.get(position);
        reminders.remove(position);
        // Notify the adapter that the data set has changed
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        return text;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewReminder;
        private final ImageButton imageButtonRemove;
        private final RemindersEditAdapter adapter;

        public ReminderViewHolder(@NonNull View itemView, RemindersEditAdapter adapter) {
            super(itemView);
            textViewReminder = itemView.findViewById(R.id.textViewReminder);
            imageButtonRemove = itemView.findViewById(R.id.imageButtonRemove);
            this.adapter = adapter;

            imageButtonRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Remove the item from the list and the database
                    String reminder = adapter.removeItem(position);
                    long locationID = adapter.locationID;
                    DatabaseManager.getInstance(itemView.getContext()).removeReminder(reminder,locationID);
                }
            });
        }

        public void bind(String reminder) {
            textViewReminder.setText(reminder);
        }
    }
}
