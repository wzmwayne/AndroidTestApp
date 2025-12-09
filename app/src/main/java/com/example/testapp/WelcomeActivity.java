package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.utils.PasswordManager;
import com.example.testapp.utils.PermissionManager;
import com.example.testapp.utils.DebugOverlayManager;
import com.example.testapp.utils.DebugClickDetector;

public class WelcomeActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    private PermissionManager permissionManager;
    private DebugOverlayManager debugOverlayManager;
    private Button submitButton;
    private TextView titleText;
    
    // 左上角连击检测
    private long lastTouchTime = 0;
    private int touchCount = 0;
    private static final int REQUIRED_TOUCHES = 5;
    private static final long TOUCH_TIMEOUT = 2000; // 2秒内

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        passwordManager = new PasswordManager(this);
        permissionManager = new PermissionManager(this);
        debugOverlayManager = DebugOverlayManager.getInstance(this);

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
        // 直接进入主界面，跳过权限检查
        try {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            // 显示调试悬浮窗
            debugOverlayManager.updateDebugInfo("启动MainActivity失败: " + e.getMessage());
            debugOverlayManager.showDebugOverlay();
            
            // 如果启动MainActivity失败，尝试启动权限申请界面
            try {
                startActivity(new Intent(this, PermissionRequestActivity.class));
                finish();
            } catch (Exception ex) {
                ex.printStackTrace();
                // 显示调试悬浮窗
                debugOverlayManager.updateDebugInfo("启动PermissionRequestActivity失败: " + ex.getMessage());
                debugOverlayManager.showDebugOverlay();
                // 如果都失败，显示错误信息
                android.widget.Toast.makeText(this, "启动失败，请重试", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 检测左上角连击
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            float x = ev.getX();
            float y = ev.getY();
            
            // 检查是否在左上角区域（100x100像素）
            if (x <= 100 && y <= 100) {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime - lastTouchTime > TOUCH_TIMEOUT) {
                    touchCount = 1;
                } else {
                    touchCount++;
                }
                
                lastTouchTime = currentTime;
                
                if (touchCount >= REQUIRED_TOUCHES) {
                    // 显示调试悬浮窗
                    debugOverlayManager.showDebugOverlay();
                    android.widget.Toast.makeText(this, "调试面板已开启", android.widget.Toast.LENGTH_SHORT).show();
                    touchCount = 0;
                }
            }
        }
        
        return super.dispatchTouchEvent(ev);
    }
}