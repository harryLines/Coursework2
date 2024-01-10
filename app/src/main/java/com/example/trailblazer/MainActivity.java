package com.example.trailblazer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREF_SELECTED_TAB_INDEX = "selected_tab_index";
    private int currentTabIndex = 0;
    ViewPager2 viewPager;
    TabLayout tabLayout;

    /**
     * Performs initialization tasks, such as setting the theme, initializing the layout,
     * handling location permission, and configuring the ViewPager and TabLayout.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);
        setContentView(R.layout.activity_main);

        checkLocationPermission();

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        currentTabIndex = getPreferences(MODE_PRIVATE).getInt(PREF_SELECTED_TAB_INDEX, 0);
        viewPager.setCurrentItem(currentTabIndex, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentTabIndex = position;

                // Save the selected tab index
                getPreferences(MODE_PRIVATE).edit().putInt(PREF_SELECTED_TAB_INDEX, currentTabIndex).apply();
            }
        });

        // Check if there is an intent and if it contains the extra data
        if (getIntent() != null && getIntent().hasExtra("fragmentToShow") || getIntent().hasExtra("stopLogging")) {
            String fragmentTag = getIntent().getStringExtra("fragmentToShow");

            // Navigate to the desired fragment based on the tag or identifier
            if (Objects.requireNonNull(fragmentTag).equals("Logging")) {
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                if (getIntent().hasExtra("stopLogging")) {
                    boolean stopLogging = getIntent().getBooleanExtra("stopLogging", false);
                    if (stopLogging) {
                        Intent serviceIntent = new Intent(this, MovementTrackerService.class);
                        this.stopService(serviceIntent);
                    }
                }
            }
        } else {

            ViewPagerAdapter adapter = new ViewPagerAdapter(this);

            adapter.addFragment(new HomeFragment(), "Home");
            adapter.addFragment(new LocationFragment(), "Saved Locations");
            adapter.addFragment(new LoggingFragment(), "Logging");
            adapter.addFragment(new TripsFragment(), "Trip History");
            adapter.addFragment(new ProgressFragment(), "Progress");
            adapter.addFragment(new GoalsFragment(), "Goals");

            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(R.layout.tab_icon);
                    ImageView tabIcon = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tabIcon);

                    // Set the appropriate icon for each tab
                    switch (i) {
                        case 0:
                            tabIcon.setImageResource(R.drawable.home_tab_icon);
                            break;
                        case 1:
                            tabIcon.setImageResource(R.drawable.location_tab_icon);
                            break;
                        case 2:
                            tabIcon.setImageResource(R.drawable.log_tab_icon);
                            break;
                        case 3:
                            tabIcon.setImageResource(R.drawable.trips_tab_icon);
                            break;
                        case 4:
                            tabIcon.setImageResource(R.drawable.progress_tab_icon);
                            break;
                        case 5:
                            tabIcon.setImageResource(R.drawable.goals_tab_icon);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Checks for and requests location permission if not granted.
     */
    private void checkLocationPermission() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission is not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Callback method invoked when permission request results are received.
     *
     * @param requestCode  The code that was specified when requesting permissions.
     * @param permissions  The requested permissions.
     * @param grantResults The results of the permission requests.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                // You can start location-related tasks here if needed
            } else {
                Log.e("LOCATION", "Location permission denied");
                // Handle the case where the user denied the permission
            }
        }
    }

    /**
     * Callback method invoked when a new intent is received by the activity.
     *
     * @param intent The new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && (intent.hasExtra("fragmentToShow") || intent.hasExtra("stopLogging"))) {
            String fragmentTag = intent.getStringExtra("fragmentToShow");

            // Navigate to the desired fragment based on the tag or identifier
            if ("Logging".equals(fragmentTag)) {
                TabLayout tabLayout = findViewById(R.id.tabLayout);
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                if (intent.hasExtra("stopLogging")) {
                    boolean stopLogging = intent.getBooleanExtra("stopLogging", false);
                    if (stopLogging) {
                        Intent serviceIntent = new Intent(this, MovementTrackerService.class);
                        stopService(serviceIntent);
                    }
                }
            }
        }
    }

    /**
     * Callback method for creating the options menu.
     *
     * @param menu The options menu to be inflated.
     * @return `true` if the menu was successfully inflated, `false` otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Callback method for handling menu item selection.
     *
     * @param item The selected menu item.
     * @return `true` if the item's action was handled, `false` otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Open SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intent);
            return true;
        }

        if(id== R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            settingsLauncher.launch(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ActivityResultLauncher for handling startActivityForResult results.
     */
    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
                ThemeManager.applyTheme(this, currentTheme);
                if (result.getResultCode() == RESULT_OK) {
                    // Recreate the activity to apply the theme changes immediately
                    recreate();
                }
            }
    );
}
