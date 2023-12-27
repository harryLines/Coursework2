package com.example.trailblazer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailblazer.databinding.GoalListItemLayoutBinding;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder> {
    private List<Goal> goals;

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final GoalListItemLayoutBinding binding;

        ViewHolder(GoalListItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Goal goal) {
            // Bind the goal to the custom layout
            binding.setGoal(goal);
            binding.executePendingBindings();
        }
    }
}
