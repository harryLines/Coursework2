package com.example.trailblazer.ui;

import static dagger.hilt.android.internal.Contexts.getApplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.trailblazer.data.DatabaseManager;
import com.example.trailblazer.data.Goal;
import com.example.trailblazer.data.GoalRepository;

import java.util.List;

public class GoalsFragmentViewModel extends ViewModel {
    private GoalRepository goalRepository;
    private LiveData<List<Goal>> goalsLiveData;

    public GoalsFragmentViewModel() {
        goalRepository = new GoalRepository(DatabaseManager.getInstance(getApplication()).goalDao());
        goalsLiveData = goalRepository.loadGoals();
    }

    public LiveData<List<Goal>> getGoalsLiveData() {
        return goalsLiveData;
    }

    public void addNewGoal(Goal goal) {
        goalRepository.addNewGoal(goal, new GoalRepository.GoalInsertCallback() {
            @Override
            public void onGoalInserted(long goalId) {
                // Handle goal insertion success
            }

            @Override
            public void onInsertFailed() {
                // Handle insertion failure
            }
        });
    }

    // Other ViewModel methods...
}
