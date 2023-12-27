package com.example.trailblazer;

public class Goal {

    public static final int TIMEFRAME_DAY = 0;
    public static final int TIMEFRAME_WEEK = 1;
    public static final int TIMEFRAME_MONTH = 2;
    String metricType;

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public int getNumberOfTimeframes() {
        return numberOfTimeframes;
    }

    public void setNumberOfTimeframes(int numberOfTimeframes) {
        this.numberOfTimeframes = numberOfTimeframes;
    }

    public int getTimeframeType() {
        return timeframeType;
    }

    public void setTimeframeType(int timeframeType) {
        this.timeframeType = timeframeType;
    }

    public double getProgress() {
        return progress;
    }

    public int getProgressAsPercentage() {
        return (int) (progress/ target);
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    int numberOfTimeframes;
    int timeframeType;
    double progress;
    double target;

}
