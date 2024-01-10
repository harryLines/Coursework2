package com.example.trailblazer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * An activity for managing and displaying various settings and preferences to the user.
 * This activity hosts a {@link SettingsFragment} to display and interact with settings options.
 */
public class SettingsActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created. Sets the activity's theme, content view,
     * and loads the {@link SettingsFragment}.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);
        setContentView(R.layout.activity_settings);
        loadFragment(new SettingsFragment());
    }

    /**
     * Loads a specified fragment into the activity's fragment container.
     *
     * @param fragment The fragment to load.
     */
    void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Called to initialize the contents of the options menu.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false, the menu will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Called when an item in the options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return Returns false to allow normal menu processing to proceed,
     * true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            // Handle the back button press
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user presses the device's back button.
     * Overrides the default behavior to handle fragment navigation.
     */
    @Override
    public void onBackPressed() {
        // Check if the current fragment is SettingsFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settingsContainer);
        if (currentFragment instanceof SettingsFragment) {
            // Finish the activity if the current fragment is SettingsFragment
            setResult(RESULT_OK);
            finish();
        } else {
            // Otherwise, let the default behavior handle the back button press
            super.onBackPressed();
        }
    }
}
