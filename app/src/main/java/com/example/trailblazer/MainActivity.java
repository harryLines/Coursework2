package com.example.trailblazer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        // Check if there is an intent and if it contains the extra data
        if (getIntent() != null && getIntent().hasExtra("fragmentToShow") || getIntent().hasExtra("stopLogging")) {
            String fragmentTag = getIntent().getStringExtra("fragmentToShow");
            Log.d("BUTTON", "NOT");

            // Navigate to the desired fragment based on the tag or identifier
            if (fragmentTag.equals("Logging")) {
                Log.d("BUTTON", "PRESSED NOT");
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                if (getIntent().hasExtra("stopLogging")) {
                    Log.d("BUTTON", "PRESSED NOT BUTTON");
                    boolean stopLogging = getIntent().getBooleanExtra("stopLogging", false);
                    if (stopLogging) {
                        Intent serviceIntent = new Intent(this, MovementTrackerService.class);
                        this.stopService(serviceIntent);
                    }
                }
            }
        } else {

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(new HomeFragment(), null);
            adapter.addFragment(new LoggingFragment(), null);
            adapter.addFragment(new LocationFragment(), null);
            adapter.addFragment(new TripsFragment(), null);
            adapter.addFragment(new ProgressFragment(), null);

            viewPager.setAdapter(adapter);

            tabLayout.setupWithViewPager(viewPager);

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(R.layout.tab_icon);
                    ImageView tabIcon = tab.getCustomView().findViewById(R.id.tabIcon);

                    // Set the appropriate icon for each tab
                    switch (i) {
                        case 0:
                            tabIcon.setImageResource(R.drawable.home_tab_icon);
                            break;
                        case 1:
                            tabIcon.setImageResource(R.drawable.log_tab_icon);
                            break;
                        case 2:
                            tabIcon.setImageResource(R.drawable.location_tab_icon);
                            break;
                        case 3:
                            tabIcon.setImageResource(R.drawable.trips_tab_icon);
                            break;
                        case 4:
                            tabIcon.setImageResource(R.drawable.progress_tab_icon);
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && (intent.hasExtra("fragmentToShow") || intent.hasExtra("stopLogging"))) {
            String fragmentTag = intent.getStringExtra("fragmentToShow");
            Log.d("BUTTON", "NOT");

            // Navigate to the desired fragment based on the tag or identifier
            if ("Logging".equals(fragmentTag)) {
                Log.d("BUTTON", "PRESSED NOT");
                TabLayout tabLayout = findViewById(R.id.tabLayout);
                tabLayout.selectTab(tabLayout.getTabAt(2));
            } else {
                if (intent.hasExtra("stopLogging")) {
                    Log.d("BUTTON", "PRESSED NOT BUTTON");
                    boolean stopLogging = intent.getBooleanExtra("stopLogging", false);
                    if (stopLogging) {
                        Intent serviceIntent = new Intent(this, MovementTrackerService.class);
                        stopService(serviceIntent);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Open SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
