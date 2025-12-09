package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.testapp.utils.PasswordManager;
import com.example.testapp.utils.PermissionManager;
import com.example.testapp.utils.DebugOverlayManager;
import com.example.testapp.utils.DebugClickDetector;

public class WelcomeActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    private PermissionManager permissionManager;
    private DebugOverlayManager debugOverlayManager;
    private Button submitButton;
    private Button permissionSettingsButton;
    private TextView titleText;
    
    // 权限状态显示
    private TextView overlayStatus;
    private TextView usageStatsStatus;
    private TextView batteryStatus;
    private TextView accessibilityStatus;
    
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
        permissionSettingsButton = findViewById(R.id.permissionSettingsButton);
        
        // 权限状态显示
        overlayStatus = findViewById(R.id.overlayStatus);
        usageStatsStatus = findViewById(R.id.usageStatsStatus);
        batteryStatus = findViewById(R.id.batteryStatus);
        accessibilityStatus = findViewById(R.id.accessibilityStatus);
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
        
        // 设置权限项点击事件
        setupPermissionClickListeners();
        
        // 更新权限状态
        updatePermissionStatus();
    }
    
    private void setupPermissionClickListeners() {
        // 悬浮窗权限
        findViewById(R.id.overlayPermissionItem).setOnClickListener(v -> {
            Intent intent = permissionManager.getOverlayPermissionIntent();
            if (intent != null) {
                startActivityForResult(intent, 1001);
            }
        });
        
        // 使用情况访问权限
        findViewById(R.id.usageAccessItem).setOnClickListener(v -> {
            Intent intent = permissionManager.getUsageStatsPermissionIntent();
            startActivityForResult(intent, 1002);
        });
        
        // 电池优化权限
        findViewById(R.id.batteryOptimizationItem).setOnClickListener(v -> {
            Intent intent = permissionManager.getBatteryOptimizationPermissionIntent();
            if (intent != null) {
                startActivityForResult(intent, 1003);
            }
        });
        
        // 无障碍服务
        findViewById(R.id.accessibilityServiceItem).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 1004);
        });
        
        // 权限设置按钮
        findViewById(R.id.permissionSettingsButton).setOnClickListener(v -> {
            startActivity(new Intent(this, PermissionRequestActivity.class));
        });
    }
    
    private void updatePermissionStatus() {
        // 更新悬浮窗权限状态
        if (permissionManager.hasOverlayPermission()) {
            overlayStatus.setText("已授予");
            overlayStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            overlayStatus.setText("未授予");
            overlayStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        
        // 更新使用情况访问权限状态
        if (permissionManager.hasUsageStatsPermission()) {
            usageStatsStatus.setText("已授予");
            usageStatsStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            usageStatsStatus.setText("未授予");
            usageStatsStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        
        // 更新电池优化权限状态
        if (permissionManager.hasBatteryOptimizationPermission()) {
            batteryStatus.setText("已授予");
            batteryStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            batteryStatus.setText("未授予");
            batteryStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
        
        // 更新无障碍服务状态
        if (isAccessibilityServiceEnabled()) {
            accessibilityStatus.setText("已启用");
            accessibilityStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            accessibilityStatus.setText("未启用");
            accessibilityStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }
    
    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String serviceName = getPackageName() + "/" + 
                    "com.example.testapp.services.AppBlockAccessibilityService";
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessibilityService = splitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 返回后更新权限状态
        updatePermissionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 界面重新显示时更新权限状态
        updatePermissionStatus();
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