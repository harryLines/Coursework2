package com.example.trailblazer;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemindersEditAdapter extends RecyclerView.Adapter<RemindersEditAdapter.ReminderViewHolder> {
    private List<Reminder> reminders;

    public RemindersEditAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }
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
        return reminders.size();
    }

    public Reminder removeItem(int position) {
        // Remove the item from the list
        Reminder reminder = reminders.get(position);
        reminders.remove(position);
        // Notify the adapter that the data set has changed
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
        return reminder;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewReminder;

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
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        database.reminderDao().removeReminder(reminder.getId());
                        //Background work here
                        handler.post(() -> {
                            //UI Thread work here
                        });
                    });
                }
            });
        }

        public void bind(String reminder) {
            textViewReminder.setText(reminder);
        }
    }
}
