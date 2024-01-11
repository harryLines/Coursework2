package com.example.trailblazer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        return goalDao.loadGoals();
    }

    /**
     * Loads goals from the database, updates their progress, and saves the updates.
     *
     * @param calories The number of calories burned.
     * @param distance The distance covered.
     * @param steps    The number of steps taken.
     */
    public void loadAndUpdateGoals(double calories, double distance, int steps) {
        executor.execute(() -> {
            // Load goals synchronously on a background thread
            List<Goal> currentGoals = goalDao.loadGoalsSync(); // Replace loadGoalsSync with the actual method name to load goals synchronously

            // Update the progress of the goals
            List<Goal> updatedGoals = updateProgress(currentGoals, calories, distance, steps);

            // Save the updated goals back to the database
            goalDao.updateGoals(updatedGoals);
        });
    }

    /**
     * Updates the progress of fitness goals based on activity data.
     *
     * @param currentGoals      The list of current fitness goals.
     * @param burnedCalories    The number of calories burned during the activity.
     * @param distanceCovered   The distance covered during the activity.
     * @param stepsTaken        The number of steps taken during the activity.
     * @return The updated list of fitness goals.
     */
    private List<Goal> updateProgress(List<Goal> currentGoals, double burnedCalories, double distanceCovered, int stepsTaken) {
        if (currentGoals != null) {

            // Iterate through the goals and update them
            for (Goal goal : currentGoals) {
                switch (goal.getMetricType()) {
                    case Goal.METRIC_CALORIES:
                        goal.setProgress(goal.getProgress() + burnedCalories);
                        Log.d("CALROEISPROGRSS", String.valueOf(goal.getProgress() + burnedCalories));
                        break;
                    case Goal.METRIC_KILOMETERS:
                        goal.setProgress(goal.getProgress() + distanceCovered);
                        break;
                    case Goal.METRIC_STEPS:
                        goal.setProgress(goal.getProgress() + stepsTaken);
                        break;
                    default:
                }
                if (goal.getProgress() >= goal.getTarget()) {
                    goal.setComplete();
                }
            }
        }
        return currentGoals;
    }


    public interface GoalLoadCallback {
        void onGoalsLoaded(List<Goal> currentGoals);
        void onInsertFailed();
    }

    public interface GoalInsertCallback {
        void onGoalInserted(long goalId);
        void onInsertFailed();
    }

}
