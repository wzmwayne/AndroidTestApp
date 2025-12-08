package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.utils.PasswordManager;
import com.example.testapp.utils.PermissionManager;

public class WelcomeActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    private PermissionManager permissionManager;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button submitButton;
    private TextView titleText;
    private boolean isSettingPassword = false;

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
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        submitButton = findViewById(R.id.submitButton);
    }

    private void setupUI() {
        if (passwordManager.isFirstTime()) {
            // 首次使用，设置密码
            isSettingPassword = true;
            titleText.setText(R.string.set_password);
            confirmPasswordInput.setVisibility(View.VISIBLE);
            submitButton.setText(R.string.set_password);
        } else {
            // 已有密码，验证身份
            isSettingPassword = false;
            titleText.setText(R.string.enter_password);
            confirmPasswordInput.setVisibility(View.GONE);
            submitButton.setText(R.string.unlock);
        }

        submitButton.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String password = passwordInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.password_hint, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSettingPassword) {
            handlePasswordSetup(password);
        } else {
            handlePasswordVerification(password);
        }
    }

    private void handlePasswordSetup(String password) {
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        if (!passwordManager.isValidPassword(password)) {
            Toast.makeText(this, R.string.password_length_error, Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.password_not_match, Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // 防止在设置密码时闪退
            passwordManager.setPassword(password);
            passwordManager.setFirstTimeComplete();
            
            // 设置完密码后，请求权限
            requestPermissions();
        } catch (Exception e) {
            Toast.makeText(this, "设置密码失败，请重试", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void handlePasswordVerification(String password) {
        if (!passwordManager.checkPassword(password)) {
            Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
            return;
        }
        
        // 密码正确，进入主界面
        startActivity(new Intent(this, MainActivity.class));
        finish();
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