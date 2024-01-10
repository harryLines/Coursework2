package com.example.trailblazer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.databinding.GoalListItemLayoutBinding;

import java.util.List;

/**
 * The GoalsAdapter class is responsible for adapting a list of goals to a RecyclerView.
 * It inflates custom goal item layouts and binds goals to the layouts.
 */
public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder> {
    private final List<Goal> goals;

    /**
     * Constructs a GoalsAdapter with the provided list of goals.
     *
     * @param goals The list of goals to be displayed in the RecyclerView.
     */
    public GoalsAdapter(List<Goal> goals) {
        this.goals = goals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom goal item layout
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        GoalListItemLayoutBinding binding = GoalListItemLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    /**
     * The ViewHolder class represents a view holder for individual goal items in the RecyclerView.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final GoalListItemLayoutBinding binding;

        /**
         * Constructs a ViewHolder with the provided binding.
         *
         * @param binding The binding for the goal item layout.
         */
        ViewHolder(GoalListItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a goal to the custom layout.
         *
         * @param goal The goal to be bound to the layout.
         */
        void bind(Goal goal) {
            // Bind the goal to the custom layout
            binding.setGoal(goal);
            binding.executePendingBindings();
        }
    }
}
