package com.example.trailblazer.ui;

import static dagger.hilt.android.internal.Contexts.getApplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.trailblazer.data.DatabaseManager;
import com.example.trailblazer.data.Goal;
import com.example.trailblazer.data.GoalRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GoalsFragmentViewModel extends ViewModel {
    private final GoalRepository goalRepository;
    private LiveData<List<Goal>> goalsLiveData;
    @Inject
    public GoalsFragmentViewModel(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
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
