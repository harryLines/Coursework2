package com.example.TrailBlazer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SettingsActivity extends AppCompatActivity {

    // Load the selected theme from SharedPreferences
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);
        setContentView(R.layout.activity_settings);
        loadFragment(new SettingsFragment());
    }

    void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

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

    @Override
    public void onBackPressed() {
        // Check if the current fragment is SettingsFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.settingsContainer);
        if (currentFragment instanceof SettingsFragment) {
            // Finish the activity if the current fragment is SettingsFragment
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Otherwise, let the default behavior handle the back button press
            super.onBackPressed();
        }
    }
}
