package com.example.trailblazer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import com.example.trailblazer.R;

/**
 * The ThemeManager class provides functionalities to manage and apply themes in an Android application.
 * It allows saving and retrieving user-selected themes and applying them to activities.
 */
public class ThemeManager {
    private static final String PREF_SELECTED_THEME = "selectedTheme";

    /**
     * Applies the specified theme to the given activity.
     *
     * @param activity The activity to which the theme is to be applied.
     * @param themeId The resource ID of the theme to be applied.
     */
    public static void applyTheme(Activity activity, int themeId) {
        activity.setTheme(themeId);
    }

    /**
     * Retrieves the currently selected theme from shared preferences.
     *
     * @param context The context from which to access the preferences.
     * @return The resource ID of the selected theme.
     */
    public static int getSelectedTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("themePreferences", Context.MODE_PRIVATE);
        return preferences.getInt(PREF_SELECTED_THEME, R.style.AppTheme_Light);
    }

    /**
     * Saves the selected theme to shared preferences.
     *
     * @param context The context from which to access the preferences.
     * @param themeId The resource ID of the theme to be saved.
     */
    public static void saveSelectedTheme(Context context, int themeId) {
        SharedPreferences preferences = context.getSharedPreferences("themePreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_SELECTED_THEME, themeId);
        editor.apply();
    }

    // Constants representing various theme resource IDs
    public static final int THEME_LIGHT = R.style.AppTheme_Light;
    public static final int THEME_DARK = R.style.AppTheme_Dark;
    public static final int THEME_ORANGE = R.style.AppTheme_Orange;
    public static final int THEME_GREEN = R.style.AppTheme_Green;
    public static final int THEME_PURPLE = R.style.AppTheme_Purple;
    public static final int THEME_RED = R.style.AppTheme_Red;

    /**
     * Returns an array of all the theme resource IDs.
     *
     * @return An array of integers representing the resource IDs of all available themes.
     */
    public static int[] getAllThemes() {
        return new int[]{THEME_LIGHT, THEME_DARK, THEME_ORANGE, THEME_GREEN, THEME_PURPLE, THEME_RED};
        // Add more themes to the array as needed
    }

    /**
     * Retrieves the accent color from the current theme.
     *
     * @param context The context used to access the theme attributes.
     * @return The color value of the accent color in the current theme.
     */
    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }
}
