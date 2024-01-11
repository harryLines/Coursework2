package com.example.trailblazer.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.trailblazer.data.Trip;
import com.example.trailblazer.data.TripRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeFragmentViewModel extends ViewModel {
    private final MutableLiveData<Boolean> walkingChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> runningChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> cyclingChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> distanceChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> caloriesChecked = new MutableLiveData<>(false);
    private final MutableLiveData<Double> avgWalkSpeed = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> avgRunSpeed = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> avgCycleSpeed = new MutableLiveData<>(0.0);
    private final TripRepository tripRepository;
    private final MutableLiveData<List<Trip>> tripHistoryLiveData = new MutableLiveData<>();

    @Inject
    public HomeFragmentViewModel(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
        loadTripHistory();
    }

    // Getter methods for LiveData
    public LiveData<Boolean> isWalkingChecked() {
        return walkingChecked;
    }

    public LiveData<Boolean> isRunningChecked() {
        return runningChecked;
    }

    public LiveData<Boolean> isCyclingChecked() {
        return cyclingChecked;
    }

    // Setter methods for updating checkbox states
    public void setWalkingChecked(boolean checked) {
        walkingChecked.setValue(checked);
    }

    public void setRunningChecked(boolean checked) {
        runningChecked.setValue(checked);
    }

    public void setCyclingChecked(boolean checked) {
        cyclingChecked.setValue(checked);
    }
    public LiveData<Double> getAvgWalkSpeed() {
        return avgWalkSpeed;
    }

    public LiveData<Double> getAvgRunSpeed() {
        return avgRunSpeed;
    }

    public LiveData<Double> getAvgCycleSpeed() {
        return avgCycleSpeed;
    }

    // Setter methods for updating average speed values
    public void setAvgWalkSpeed(double speed) {
        avgWalkSpeed.setValue(speed);
    }

    public void setAvgRunSpeed(double speed) {
        avgRunSpeed.setValue(speed);
    }

    public void setAvgCycleSpeed(double speed) {
        avgCycleSpeed.setValue(speed);
    }

    public LiveData<Boolean> isDistanceChecked() {
        return distanceChecked;
    }

    public void setDistanceChecked(boolean distanceChecked) {
        this.distanceChecked.setValue(distanceChecked);
    }

    public LiveData<Boolean> isCaloriesChecked() {
        return caloriesChecked;
    }

    public void setCaloriesChecked(boolean caloriesChecked) {
        this.caloriesChecked.setValue(caloriesChecked);
    }

    private void loadTripHistory() {
        // Use your repository to load trip history. Assuming it returns LiveData
        LiveData<List<Trip>> tripHistory = tripRepository.loadTripHistory();
        tripHistory.observeForever(new Observer<List<Trip>>() {
            @Override
            public void onChanged(List<Trip> trips) {
                tripHistoryLiveData.setValue(trips);
            }
        });
    }

    public LiveData<List<Trip>> getTripHistory() {
        return tripHistoryLiveData;
    }
}
