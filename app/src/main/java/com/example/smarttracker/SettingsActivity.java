package com.example.smarttracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    Switch switchDarkMode;
    Switch switchReminders;
    Button btnSaveSettings;
    SharedPreferences settingsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set up the action bar with an up/back arrow
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        switchDarkMode = (Switch) findViewById(R.id.switchDarkMode);
        switchReminders = (Switch) findViewById(R.id.switchReminders);
        btnSaveSettings = (Button) findViewById(R.id.btnSaveSettings);

        //load the previously saved values from SharedPreferences
        settingsPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean savedDarkMode = settingsPrefs.getBoolean("dark_mode", false);
        boolean savedReminders = settingsPrefs.getBoolean("reminders_enabled", false);
        switchDarkMode.setChecked(savedDarkMode);
        switchReminders.setChecked(savedReminders);

        //save button writes the new value with commit()
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDark = switchDarkMode.isChecked();
                boolean remindersEnabled = switchReminders.isChecked();
                SharedPreferences.Editor editor = settingsPrefs.edit();
                editor.putBoolean("dark_mode", isDark);
                editor.putBoolean("reminders_enabled", remindersEnabled);
                editor.commit();

                if (isDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //handle the up/back arrow tap in the action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
