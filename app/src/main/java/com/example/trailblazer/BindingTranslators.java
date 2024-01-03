package com.example.trailblazer;

import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import java.text.DecimalFormat;
import java.util.Locale;

public class BindingTranslators {
    @BindingAdapter("android:text")
    public static void setLongText(TextView view, long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);

        // Set the formatted time to the TextView
        view.setText(formattedTime);
    }


}
