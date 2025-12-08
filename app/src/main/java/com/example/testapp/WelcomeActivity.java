package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.utils.PasswordManager;
import com.example.testapp.utils.PermissionManager;

public class WelcomeActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    private PermissionManager permissionManager;
    private Button submitButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        passwordManager = new PasswordManager(this);
        permissionManager = new PermissionManager(this);

        initViews();
        setupUI();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        submitButton = findViewById(R.id.submitButton);
    }

    private void setupUI() {
        if (passwordManager.isFirstTime()) {
            // 首次使用
            titleText.setText("欢迎使用应用");
            submitButton.setText("开始使用");
        } else {
            // 已使用过
            titleText.setText("欢迎回来");
            submitButton.setText("进入应用");
        }

        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        // 标记首次使用完成
        passwordManager.setFirstTimeComplete();
        
        // 请求权限
        requestPermissions();
    }

    private void requestPermissions() {
        if (!permissionManager.hasAllPermissions()) {
            // 进入权限申请界面
            startActivity(new Intent(this, PermissionRequestActivity.class));
            finish();
        } else {
            // 已有所有权限，直接进入主界面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}