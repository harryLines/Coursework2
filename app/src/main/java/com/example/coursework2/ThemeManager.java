package com.example.coursework2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

public class ThemeManager {
    private static final String PREF_SELECTED_THEME = "selectedTheme";

    public static void applyTheme(Activity activity, int themeId) {
        activity.setTheme(themeId);
    }

    public static int getSelectedTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("themePreferences", Context.MODE_PRIVATE);
        return preferences.getInt(PREF_SELECTED_THEME, R.style.AppTheme_Light);
    }

    public static void saveSelectedTheme(Context context, int themeId) {
        SharedPreferences preferences = context.getSharedPreferences("themePreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_SELECTED_THEME, themeId);
        editor.apply();
    }

    public static final int THEME_LIGHT = R.style.AppTheme_Light;
    public static final int THEME_DARK = R.style.AppTheme_Dark;
    public static final int THEME_ORANGE = R.style.AppTheme_Orange;
    public static final int THEME_GREEN = R.style.AppTheme_Green;
    public static final int THEME_PURPLE = R.style.AppTheme_Purple;
    public static final int THEME_RED = R.style.AppTheme_Red;

    public static int[] getAllThemes() {
        return new int[]{THEME_LIGHT, THEME_DARK, THEME_ORANGE, THEME_GREEN, THEME_PURPLE, THEME_RED};
        // Add more themes to the array as needed
    }

    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }
}
