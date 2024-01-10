package com.example.trailblazer;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for managing Goal entities.
 */
public class GoalRepository {
    private final GoalDao goalDao;
    private final Executor executor;

    /**
     * Constructor for GoalRepository.
     *
     * @param goalDao DAO for accessing goal data.
     */
    public GoalRepository(GoalDao goalDao) {
        this.goalDao = goalDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Adds a new goal to the database.
     *
     * @param goal The goal to be added.
     */
    public void addNewGoal(Goal goal, GoalInsertCallback callback) {
        executor.execute(() -> {
            long goalId = goalDao.addNewGoal(goal);
            // Run on the main thread
            new Handler(Looper.getMainLooper()).post(() -> callback.onGoalInserted(goalId));
        });
    }
    /**
     * Updates a list of goals in the database.
     *
     * @param updatedGoals The list of goals to be updated.
     */
    public void updateGoals(List<Goal> updatedGoals) {
        executor.execute(() -> goalDao.updateGoals(updatedGoals));
    }

    /**
     * Retrieves all goals from the database.
     *
     * @return A LiveData list of all goals.
     */
    public LiveData<List<Goal>> loadGoals() {
        // Assuming you want to observe changes in the database and update the UI accordingly
        // Change return type to LiveData if you want real-time updates in the UI
        return goalDao.loadGoals();
    }
    public interface GoalInsertCallback {
        void onGoalInserted(long goalId);
        void onInsertFailed();
    }

}
