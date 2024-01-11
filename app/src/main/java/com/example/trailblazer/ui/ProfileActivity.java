package com.example.trailblazer.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trailblazer.R;

/**
 * The `ProfileActivity` class allows users to view and edit their profile information,
 * including their name and weight. Users can make changes and save them to SharedPreferences.
 */
public class ProfileActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextWeight;
    private SharedPreferences sharedPreferences;
    private static final String PREF_USER_DETAILS = "user_details";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_WEIGHT = "weight";

    /**
     * Initializes the activity and sets its layout. It also loads existing user data
     * from SharedPreferences and sets click listeners for the Save Details button.
     *
     * @param savedInstanceState A Bundle containing the saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the selected theme
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);

        // Set the layout for the activity
        setContentView(R.layout.activity_profile);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextWeight = findViewById(R.id.editTextWeight);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Load existing user data
        loadUserData();

        // Set click listener for the Save Details button
        Button saveDetailsButton = findViewById(R.id.btnSaveDetails);
        saveDetailsButton.setOnClickListener(v -> {
            // Save the updated data
            saveUserData();
            setResult(RESULT_OK);
            finish();
        });
    }

    /**
     * Loads existing user data from SharedPreferences and sets it to the corresponding views.
     */
    private void loadUserData() {
        // Load existing data from SharedPreferences
        String savedName = sharedPreferences.getString("username", "");
        float savedWeight = sharedPreferences.getFloat("weight", 0.0f);

        // Set the loaded data to the corresponding views
        editTextName.setText(savedName);
        editTextWeight.setText(String.valueOf(savedWeight));
        // You may also load and set the profile picture here if needed
    }

    /**
     * Saves the updated user data (name and weight) to SharedPreferences.
     */
    private void saveUserData() {
        String newName = editTextName.getText().toString();
        try {
            float newWeight = Float.parseFloat(editTextWeight.getText().toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, newName);
            editor.putFloat(KEY_WEIGHT, newWeight);
            editor.apply();
        } catch (NumberFormatException e) {
            throw e;
        }
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Initializes the options menu for the activity.
     *
     * @param menu The options menu in which you place your items.
     * @return `true` to display the menu, `false` to prevent it from being displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    /**
     * Handles item selections from the options menu. In this case, it handles the back button press.
     *
     * @param item The selected item from the menu.
     * @return `true` if the item is handled, `false` otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            // Handle the back button press
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
