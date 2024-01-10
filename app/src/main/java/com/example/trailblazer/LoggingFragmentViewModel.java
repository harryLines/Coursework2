package com.example.trailblazer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoggingFragmentViewModel extends ViewModel {
    private final MutableLiveData<Double> distance = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> calories = new MutableLiveData<>(0);
    private final MutableLiveData<Long> seconds = new MutableLiveData<>(0L);
    private final MutableLiveData<String> savedLocationName = new MutableLiveData<>("");
    public MutableLiveData<Integer> getWeather() {
        return weather;
    }
    private final MutableLiveData<Integer> weather = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> steps = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> walkingChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> runningChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cyclingChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> getIsTracking() {
        return isTracking;
    }
    public MutableLiveData<Double> getDistance() {
        return distance;
    }
    public MutableLiveData<Long> getSeconds() {
        return seconds;
    }
    public MutableLiveData<String> getSavedLocationName() {
        return savedLocationName;
    }
    public MutableLiveData<Integer> getSteps() {
        return steps;
    }
    public void setDistance(double distanceValue) {
        distance.setValue(distanceValue);
    }
    public void setCalories(int caloriesValue) {
        calories.setValue(caloriesValue);
    }
    public void setWeather(int weatherValue) {
        weather.setValue(weatherValue);
    }
    public void setSeconds(long secondsValue) {
        seconds.setValue(secondsValue);
    }
    public void setSavedLocationName(String locationName) {
        savedLocationName.setValue(locationName);
    }
    public void setSteps(int stepsValue) {
        steps.setValue(stepsValue);
    }
    public void setIsTracking(Boolean value) {
        isTracking.setValue(value);
    }
    public MutableLiveData<Boolean> getWalkingChecked() {
        return walkingChecked;
    }
    public void setWalkingChecked(boolean walkingChecked) {
        this.walkingChecked.setValue(walkingChecked);
    }
    public MutableLiveData<Boolean> getRunningChecked() {
        return runningChecked;
    }
    public void setRunningChecked(boolean runningChecked) {
        this.runningChecked.setValue(runningChecked);
    }
    public MutableLiveData<Boolean> getCyclingChecked() {
        return cyclingChecked;
    }
    public void setCyclingChecked(boolean cyclingChecked) {
        this.cyclingChecked.setValue(cyclingChecked);
    }
    public MutableLiveData<Integer> getCalories() {
        return calories;
    }
}
