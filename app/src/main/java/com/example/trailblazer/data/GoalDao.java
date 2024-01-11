package com.example.trailblazer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface GoalDao {
    @Insert
    long addNewGoal(Goal goal);
    @Update
    void updateGoals(List<Goal> updatedGoals);
    @Query("SELECT * FROM goals")
    LiveData<List<Goal>> loadGoals();
    @Query("SELECT * FROM goals")
    List<Goal> loadGoalsSync();

}
