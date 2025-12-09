package com.example.testapp.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AppBlockAccessibilityService extends AccessibilityService {
    private static final String TAG = "AppBlockAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : null;
            
            if (packageName != null) {
                Log.d(TAG, "Window state changed: " + packageName);
                
                // 检查是否需要拦截
                handleWindowStateChange(packageName);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
    }

    private void handleWindowStateChange(String packageName) {
        // 实时检测前台应用
        try {
            // 检查是否需要拦截该应用
            if (shouldBlockApp(packageName)) {
                Log.d(TAG, "Blocking app: " + packageName);
                performBlockAction(packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling window state change", e);
        }
    }
    
    private boolean shouldBlockApp(String packageName) {
        // 获取拦截列表
        String blockedApps = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("blacklist_apps", "");
        String whitelistApps = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("whitelist_apps", "");
        boolean isBlacklistMode = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_blacklist_mode", true);
        
        // 检查保护是否启用
        boolean protectionEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("protection_enabled", true);
        
        if (!protectionEnabled) {
            return false;
        }
        
        if (isBlacklistMode) {
            // 黑名单模式：检查是否在黑名单中
            return blockedApps.contains(packageName);
        } else {
            // 白名单模式：检查是否不在白名单中
            return !whitelistApps.isEmpty() && !whitelistApps.contains(packageName);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra("action");
            String packageName = intent.getStringExtra("package_name");
            
            if ("block_app".equals(action) && packageName != null) {
                performBlockAction(packageName);
            }
        }
        
        return START_STICKY;
    }

    private void performBlockAction(String packageName) {
        // 先尝试返回主屏幕
        goBackToHome();
        
        // 检查是否需要显示拦截通知
        boolean notificationEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("notification_enabled", true);
        
        if (notificationEnabled) {
            // 显示拦截界面
            Intent blockIntent = new Intent(this, BlockOverlayActivity.class);
            blockIntent.putExtra("package_name", packageName);
            blockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(blockIntent);
        }
    }
    
    private void goBackToHome() {
        try {
            // 返回到主屏幕
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error returning to home", e);
        }
    }
}