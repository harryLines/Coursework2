package com.example.trailblazer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class GoalsFragmentViewModel extends ViewModel {
    private MutableLiveData<List<Goal>> goalsList = new MutableLiveData<>();

    public MutableLiveData<List<Goal>> getGoalsList() {
        return goalsList;
    }

    // Function to update goalsList
    public void setGoalsList(List<Goal> goals) {
        goalsList.setValue(goals);
    }
}
