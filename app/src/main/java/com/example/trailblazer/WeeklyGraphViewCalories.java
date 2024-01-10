package com.example.trailblazer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom view to display calorie data as a bar graph on a weekly basis.
 * This view takes a list of calorie values and corresponding dates, and draws a bar graph with these values.
 */
public class WeeklyGraphViewCalories extends View {
    private List<Integer> dataPoints;
    private List<Date> dateList;
    private int themeColor;
    final Paint textPaint = new Paint();
    final Paint barPaint = new Paint();
    private int dataType;

    public WeeklyGraphViewCalories(Context context) {
        super(context);
        themeColor = ThemeManager.getAccentColor(getContext());
    }

    public WeeklyGraphViewCalories(Context context, AttributeSet attrs) {
        super(context, attrs);
        themeColor = ThemeManager.getAccentColor(getContext());
    }

    public WeeklyGraphViewCalories(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        themeColor = ThemeManager.getAccentColor(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int numPoints = dataPoints.size();

        // Set up the Paint for drawing bars
        barPaint.setColor(themeColor);
        barPaint.setStrokeWidth(5);

        // Set up the Paint for drawing text labels
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textPaint.setTextSize(48);

        // Calculate the width of each bar based on the available space
        float barWidth = (float) width / numPoints;

        // Calculate the maximum value for normalization
        float maxValue = getMaxValue(dataPoints);

        // Draw bars and labels
        for (int i = 0; i < numPoints; i++) {
            float value = dataPoints.get(i);
            float normalizedValue = (value / maxValue) * height;

            float left = i * barWidth;
            float top = height - normalizedValue;
            float right = left + barWidth;

            // Draw bar
            canvas.drawRect(left, top, right, (float) height, barPaint);

            // Draw label in the middle of the bar
            String caloriesLabel = String.format(Locale.getDefault(), "%d kcal", (int) value);

            // Rotate the canvas for vertical text
            canvas.save();
            canvas.rotate(-90, left + (barWidth / 2), (top + (float) height) / 2);

            // Ensure that the label is within the canvas bounds
            float labelX = left + (barWidth / 2) - (textPaint.measureText(caloriesLabel) / 2);
            float labelY = (top + (float) height) / 2;
            labelY = Math.max(labelY, 0);

            canvas.drawText(caloriesLabel, labelX, labelY, textPaint);

            // Restore the canvas to its original orientation
            canvas.restore();

            // Draw date label at the bottom of the bar
            String dateLabel = formatDate(dateList.get(i));
            float dateLabelX = left + (barWidth / 2) - (textPaint.measureText(dateLabel) / 2);
            float dateLabelY = (float) height + 50 + textPaint.getTextSize(); // Adjust this value for label position below the bar

            // Ensure that the date label is within the canvas bounds
            dateLabelY = Math.min(dateLabelY, getHeight() - textPaint.getTextSize());

            canvas.drawText(dateLabel, dateLabelX, dateLabelY, textPaint);
        }
    }

    /**
     * Formats the given date into a readable string.
     *
     * @param date The date to format.
     * @return A string representing the formatted date.
     */
    private String formatDate(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Format for day of the week
        String dayOfWeek = dayFormat.format(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(date);
        String todayString = dateFormat.format(new Date());

        return dateString.equals(todayString) ? "Today" : dayOfWeek;
    }

    /**
     * Finds the maximum value in a list of integers.
     *
     * @param values The list of integer values.
     * @return The maximum value found in the list.
     */
    private float getMaxValue(List<Integer> values) {
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    /**
     * Sets the data points and corresponding dates for the graph and triggers a re-draw.
     *
     * @param dataPoints A list of integers representing calorie data points.
     * @param dateList A list of Date objects corresponding to each calorie data point.
     */
    public void setDataPoints(List<Integer> dataPoints, List<Date> dateList) {
        // Reverse the order of dataPoints and dateList
        Collections.reverse(dateList);

        this.dataPoints = dataPoints;
        this.dateList = dateList;
        invalidate();
    }
}
