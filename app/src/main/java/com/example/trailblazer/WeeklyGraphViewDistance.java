package com.example.trailblazer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyGraphViewDistance extends View {
    private List<Float> dataPoints;
    private List<Date> dateList;
    private int themeColor;
    private Paint barPaint;
    private Paint textPaint;

    public WeeklyGraphViewDistance(Context context) {
        super(context);
        themeColor = ThemeManager.getAccentColor(getContext());
        barPaint = new Paint();
        textPaint = new Paint();
        init();
    }

    public WeeklyGraphViewDistance(Context context, AttributeSet attrs) {
        super(context, attrs);
        themeColor = ThemeManager.getAccentColor(getContext());
        barPaint = new Paint();
        textPaint = new Paint();
        init();
    }

    public WeeklyGraphViewDistance(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        themeColor = ThemeManager.getAccentColor(getContext());
        barPaint = new Paint();
        textPaint = new Paint();
        init();
    }

    private void init() {
        // Initialize your dataPoints here
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
            float valueInMeters = dataPoints.get(i);
            float normalizedValue = (valueInMeters / maxValue) * height;

            float left = i * barWidth;
            float top = height - normalizedValue;
            float right = left + barWidth;

            // Draw bar
            canvas.drawRect(left, top, right, (float) height, barPaint);

            // Draw label in the middle of the bar
            float valueInKilometers = valueInMeters / 1000.0f;
            String distanceLabel = String.format("%.2f km", valueInKilometers);

            // Rotate the canvas for vertical text
            canvas.save();
            canvas.rotate(-90, left + (barWidth / 2), (top + (float) height) / 2);

            // Ensure that the label is within the canvas bounds
            float labelX = left + (barWidth / 2) - (textPaint.measureText(distanceLabel) / 2);
            float labelY = (top + (float) height) / 2;
            labelY = Math.max(labelY, 0);

            canvas.drawText(distanceLabel, labelX, labelY, textPaint);

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


    private String formatDate(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Format for day of the week
        String dayOfWeek = dayFormat.format(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(date);
        String todayString = dateFormat.format(new Date());

        return dateString.equals(todayString) ? "Today" : dayOfWeek;
    }


    private float getMaxValue(List<Float> values) {
        float max = Float.MIN_VALUE;
        for (float value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    public void setDataPoints(List<Float> dataPoints, List<Date> dateList) {
        // Reverse the order of dataPoints and dateList
        Collections.reverse(dateList);

        Log.d("DataPoints", "Reversed DataPoints: " + dataPoints);
        Log.d("DateList", "Reversed DateList: " + dateList);

        this.dataPoints = dataPoints;
        this.dateList = dateList;
        invalidate(); // Trigger onDraw
    }

    public void setThemeColor(int color) {
        this.themeColor = color;
        invalidate(); // Trigger onDraw
    }

}
