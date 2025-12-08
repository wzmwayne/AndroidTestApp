package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.utils.PasswordManager;

public class SettingsActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    
    private Switch protectionSwitch;
    private Switch autoStartSwitch;
    private Switch notificationSwitch;
    private Button changePasswordButton;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        passwordManager = new PasswordManager(this);
        
        initViews();
        setupClickListeners();
        loadSettings();
    }

    private void initViews() {
        protectionSwitch = findViewById(R.id.protectionSwitch);
        autoStartSwitch = findViewById(R.id.autoStartSwitch);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        resetButton = findViewById(R.id.resetButton);
    }

    private void setupClickListeners() {
        changePasswordButton.setOnClickListener(v -> {
            // 重新设置密码
            passwordManager.setFirstTimeComplete();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        resetButton.setOnClickListener(v -> {
            // 重置所有设置
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "已重置所有设置", Toast.LENGTH_SHORT).show();
            finish();
        });

        protectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("protection_enabled", isChecked)
                    .apply();
        });

        autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("auto_start_enabled", isChecked)
                    .apply();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("notification_enabled", isChecked)
                    .apply();
        });
    }

    private void loadSettings() {
        boolean protectionEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("protection_enabled", true);
        boolean autoStartEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("auto_start_enabled", true);
        boolean notificationEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("notification_enabled", true);

        protectionSwitch.setChecked(protectionEnabled);
        autoStartSwitch.setChecked(autoStartEnabled);
        notificationSwitch.setChecked(notificationEnabled);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}