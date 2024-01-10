package com.example.trailblazer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The `ProfileActivity` class allows users to view and edit their profile information,
 * including their name and weight. Users can make changes and save them to SharedPreferences.
 */
public class ProfileActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextWeight;
    private SharedPreferences sharedPreferences;

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
        // Get the user input from the views
        String newName = editTextName.getText().toString();
        float newWeight = Float.parseFloat(editTextWeight.getText().toString());

        // Save the updated data to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", newName);
        editor.putFloat("weight", newWeight);
        editor.apply();
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
