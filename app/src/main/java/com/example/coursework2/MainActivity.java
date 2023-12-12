package com.example.coursework2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), null);
        adapter.addFragment(new LoggingFragment(), null); // Changed to LoggingFragment
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
                        tabIcon.setImageResource(R.drawable.settings_cog);
                        break;
                    case 3:
                        tabIcon.setImageResource(R.drawable.settings_cog);
                        break;
                    case 4:
                        tabIcon.setImageResource(R.drawable.settings_cog);
                        break;
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
