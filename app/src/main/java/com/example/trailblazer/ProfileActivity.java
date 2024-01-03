package com.example.trailblazer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextWeight;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int currentTheme = ThemeManager.getSelectedTheme(getApplicationContext());
        ThemeManager.applyTheme(this, currentTheme);
        setContentView(R.layout.activity_profile);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextWeight = findViewById(R.id.editTextWeight);
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Load existing data
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

    private void loadUserData() {
        // Load existing data from SharedPreferences
        String savedName = sharedPreferences.getString("username", "");
        float savedWeight = sharedPreferences.getFloat("weight", 0.0f);

        // Set the loaded data to the corresponding views
        editTextName.setText(savedName);
        editTextWeight.setText(String.valueOf(savedWeight));
        // You may also load and set the profile picture here if needed
    }

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
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}