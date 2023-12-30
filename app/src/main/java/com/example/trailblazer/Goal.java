package com.example.trailblazer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Goal {

    public static final int TIMEFRAME_DAY = 0;
    public static final int TIMEFRAME_WEEK = 1;
    public static final int TIMEFRAME_MONTH = 2;
    public static final int METRIC_CALORIES = 0;
    public static final int METRIC_KILOMETERS = 1;
    public static final int METRIC_STEPS= 2;
    int metricType;
    int numberOfTimeframes;
    int timeframeType;
    double progress;
    double target;

    public long getGoalID() {
        return goalID;
    }

    public void setGoalID(long goalID) {
        this.goalID = goalID;
    }

    long goalID;

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    Date dateCreated;

    public String getFormattedDueDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dueDate = getDueDate();
        Date currentDate = new Date(); // current date

        long timeDifference = dueDate.getTime() - currentDate.getTime();
        long daysDifference = timeDifference / (1000 * 60 * 60 * 24);

        if (daysDifference <= 0) {
            return "Expired"; // or handle expired case as needed
        } else if (daysDifference < 7) {
            return daysDifference + " days left";
        } else if (daysDifference < 30) {
            return "1 week left";
        } else {
            long monthsDifference = daysDifference / 30;
            return monthsDifference + " month" + (monthsDifference > 1 ? "s" : "") + " left";
        }
    }

    public Date getDueDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateCreated);

        switch (timeframeType) {
            case TIMEFRAME_DAY:
                calendar.add(Calendar.DAY_OF_MONTH, numberOfTimeframes);
                break;
            case TIMEFRAME_WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, numberOfTimeframes);
                break;
            case TIMEFRAME_MONTH:
                calendar.add(Calendar.MONTH, numberOfTimeframes);
                break;
            default:
                // Handle the default case if needed
        }

        return calendar.getTime();
    }

    public int getMetricType() {
        return metricType;
    }
    public Goal(long goalID, int metricType, int numberOfTimeframes, int timeframeType, double progress, double target, Date dateCreated) {
        this.metricType = metricType;
        this.numberOfTimeframes = numberOfTimeframes;
        this.timeframeType = timeframeType;
        this.progress = progress;
        this.target = target;
        this.goalID = goalID;
        this.dateCreated = dateCreated;
    }

    public String getTargetWithUnitString() {
        String unit;
        String formatSpecifier;

        switch (metricType) {
            case METRIC_CALORIES:
                unit = "kcal";
                formatSpecifier = "%.0f %s"; // Format with no decimal points
                break;
            case METRIC_KILOMETERS:
                unit = "km";
                formatSpecifier = "%.2f %s"; // Format with two decimal points
                break;
            case METRIC_STEPS:
                unit = "steps";
                formatSpecifier = "%.0f %s"; // Format with no decimal points
                break;
            default:
                unit = "";
                formatSpecifier = "%.2f %s"; // Default format with two decimal points
        }

        return String.format(formatSpecifier, target, unit);
    }

    public String getProgressWithUnitString() {
        String unit;
        String formatSpecifier;

        switch (metricType) {
            case METRIC_CALORIES:
                unit = "kcal";
                formatSpecifier = "%.0f %s"; // Format with no decimal points
                break;
            case METRIC_KILOMETERS:
                unit = "km";
                formatSpecifier = "%.2f %s"; // Format with two decimal points
                break;
            case METRIC_STEPS:
                unit = "steps";
                formatSpecifier = "%.0f %s"; // Format with no decimal points
                break;
            default:
                unit = "";
                formatSpecifier = "%.2f %s"; // Default format with two decimal points
        }

        return String.format(formatSpecifier, progress, unit);
    }

    public String getMetricTypeAsText() {
        switch (metricType) {
            case METRIC_CALORIES:
                return "Calories";
            case METRIC_KILOMETERS:
                return "Kilometers";
            case METRIC_STEPS:
                return "Steps";
            default:
                return "Unknown";
        }
    }

    public void setMetricType(int metricType) {
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

    public int getProgressAsInt() {
        return (int) progress;
    }

    public String getProgressAsPercentageString() {
        return String.format(Locale.getDefault(), "%.0f%%", (progress / target) * 100);
    }

    public int getProgressAsPercentage() {
        return target != 0 ? (int) ((progress / target) * 100) : 0;
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
}
