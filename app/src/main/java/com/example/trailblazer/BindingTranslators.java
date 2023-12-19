package com.example.trailblazer;

import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;

import java.text.DecimalFormat;
import java.util.Locale;

import kotlin.jvm.JvmStatic;

public class BindingTranslators {

    @BindingAdapter("android:text")
    public static void setDoubleText(TextView view, LiveData<Double> value) {
        // Check if the LiveData value is not null
        if (value != null && value.getValue() != null) {
            // Round the Double value to two decimal places
            double roundedValue = roundToTwoDecimalPlaces(value.getValue());

            // Set the text of the TextView to the string representation of the rounded Double value
            view.setText(String.valueOf(roundedValue));
        }
    }
    @BindingAdapter("android:text")
    public static void setLongText(TextView view, long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);

        // Set the formatted time to the TextView
        view.setText(formattedTime);
    }


    private static double roundToTwoDecimalPlaces(double value) {
        // Use DecimalFormat to round to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(value));
    }
}
