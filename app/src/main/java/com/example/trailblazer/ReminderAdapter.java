package com.example.trailblazer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private final List<String> reminders;

    public ReminderAdapter(List<String> reminders) {
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your reminder item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        // Bind the data to your reminder item views
        String reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    // ViewHolder class
    public static class ReminderViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtReminder;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your views from the reminder item layout
            txtReminder = itemView.findViewById(R.id.txtReminder);
        }

        public void bind(String reminder) {
            // Bind the data to your views
            txtReminder.setText(reminder);
        }
    }

    public void setReminders(List<String> newReminders) {
        reminders.clear();
        reminders.addAll(newReminders);
        notifyDataSetChanged();
    }
}