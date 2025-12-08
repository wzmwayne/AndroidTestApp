package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.testapp.utils.PermissionManager;
import com.example.testapp.utils.DhizukuManager;

public class PermissionRequestActivity extends AppCompatActivity {
    private PermissionManager permissionManager;
    private DhizukuManager dhizukuManager;
    
    private CardView overlayCard;
    private CardView usageStatsCard;
    private CardView batteryCard;
    private CardView accessibilityCard;
    private CardView dhizukuCard;
    
    private TextView overlayStatus;
    private TextView usageStatsStatus;
    private TextView batteryStatus;
    private TextView accessibilityStatus;
    private TextView dhizukuStatus;
    
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);

        permissionManager = new PermissionManager(this);
        dhizukuManager = new DhizukuManager(this);
        
        initViews();
        updatePermissionStatus();
        setupClickListeners();
    }

    private void initViews() {
        overlayCard = findViewById(R.id.overlayCard);
        usageStatsCard = findViewById(R.id.usageStatsCard);
        batteryCard = findViewById(R.id.batteryCard);
        accessibilityCard = findViewById(R.id.accessibilityCard);
        dhizukuCard = findViewById(R.id.dhizukuCard);
        
        overlayStatus = findViewById(R.id.overlayStatus);
        usageStatsStatus = findViewById(R.id.usageStatsStatus);
        batteryStatus = findViewById(R.id.batteryStatus);
        accessibilityStatus = findViewById(R.id.accessibilityStatus);
        dhizukuStatus = findViewById(R.id.dhizukuStatus);
        
        continueButton = findViewById(R.id.continueButton);
    }

    private void updatePermissionStatus() {
        // 更新悬浮窗权限状态
        if (permissionManager.hasOverlayPermission()) {
            overlayStatus.setText("已授予");
            overlayStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            overlayStatus.setText("未授予");
            overlayStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 更新使用情况访问权限状态
        if (permissionManager.hasUsageStatsPermission()) {
            usageStatsStatus.setText("已授予");
            usageStatsStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            usageStatsStatus.setText("未授予");
            usageStatsStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 更新电池优化权限状态
        if (permissionManager.hasBatteryOptimizationPermission()) {
            batteryStatus.setText("已授予");
            batteryStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            batteryStatus.setText("未授予");
            batteryStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 更新无障碍服务状态
        if (isAccessibilityServiceEnabled()) {
            accessibilityStatus.setText("已启用");
            accessibilityStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            accessibilityStatus.setText("未启用");
            accessibilityStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 更新Dhizuku状态
        if (dhizukuManager.isDhizukuAvailable() && dhizukuManager.hasPermission()) {
            dhizukuStatus.setText("已授权");
            dhizukuStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            dhizukuStatus.setText("未授权");
            dhizukuStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 检查是否可以继续
        continueButton.setEnabled(permissionManager.hasAllPermissions() && isAccessibilityServiceEnabled() && 
                               (dhizukuManager.isDhizukuAvailable() && dhizukuManager.hasPermission()));
    }

    private void setupClickListeners() {
        overlayCard.setOnClickListener(v -> {
            Intent intent = permissionManager.getOverlayPermissionIntent();
            if (intent != null) {
                startActivityForResult(intent, 1001);
            }
        });
        
        usageStatsCard.setOnClickListener(v -> {
            Intent intent = permissionManager.getUsageStatsPermissionIntent();
            startActivityForResult(intent, 1002);
        });
        
        batteryCard.setOnClickListener(v -> {
            Intent intent = permissionManager.getBatteryOptimizationPermissionIntent();
            if (intent != null) {
                startActivityForResult(intent, 1003);
            }
        });
        
        accessibilityCard.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 1004);
        });
        
        dhizukuCard.setOnClickListener(v -> {
            if (dhizukuManager.isDhizukuAvailable()) {
                dhizukuManager.requestPermission();
            } else {
                Intent intent = dhizukuManager.getDhizukuSettingsIntent();
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "请先安装Dhizuku", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        continueButton.setOnClickListener(v -> {
            if (permissionManager.hasAllPermissions() && isAccessibilityServiceEnabled() && 
                (dhizukuManager.isDhizukuAvailable() && dhizukuManager.hasPermission())) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "请先授予所有权限", Toast.LENGTH_SHORT).show();
            }
        });
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
}