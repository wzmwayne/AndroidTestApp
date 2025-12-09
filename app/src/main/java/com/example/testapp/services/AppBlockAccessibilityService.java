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
                Log.d(TAG, "窗口状态改变: " + packageName);
                
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
            // 确保自身不被拦截
            if (packageName.equals(getPackageName())) {
                return;
            }
            
            // 确保系统应用不被拦截
            if (isSystemApp(packageName)) {
                return;
            }
            
            // 检查是否需要拦截该应用
            if (shouldBlockApp(packageName)) {
                Log.d(TAG, "Blocking app: " + packageName);
                performBlockAction(packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling window state change", e);
        }
    }
    
    private boolean isSystemApp(String packageName) {
        try {
            android.content.pm.ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, 0);
            return (appInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return true; // 如果无法确定，默认为系统应用
        }
    }
    
    private boolean shouldBlockApp(String packageName) {
        // 不拦截自身应用
        if (getPackageName().equals(packageName)) {
            return false;
        }
        
        // 不拦截系统应用和系统界面
        if (packageName.equals("com.android.systemui") ||
            packageName.equals("com.android.launcher3") ||
            packageName.startsWith("com.android.") ||
            packageName.equals("android")) {
            return false;
        }
        
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
        
        // 添加调试日志
        Log.d(TAG, "检查应用: " + packageName);
        Log.d(TAG, "保护启用: " + protectionEnabled);
        Log.d(TAG, "模式: " + (isBlacklistMode ? "黑名单" : "白名单"));
        Log.d(TAG, "黑名单: " + blockedApps);
        Log.d(TAG, "白名单: " + whitelistApps);
        
        if (!protectionEnabled) {
            Log.d(TAG, "保护未启用，不拦截");
            return false;
        }
        
        boolean shouldBlock = false;
        if (isBlacklistMode) {
            // 黑名单模式：检查是否在黑名单中
            shouldBlock = blockedApps.contains(packageName);
            Log.d(TAG, "黑名单模式，是否拦截: " + shouldBlock);
        } else {
            // 白名单模式：检查是否不在白名单中
            shouldBlock = !whitelistApps.isEmpty() && !whitelistApps.contains(packageName);
            Log.d(TAG, "白名单模式，是否拦截: " + shouldBlock);
        }
        
        return shouldBlock;
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
        // 显示悬浮窗黑色全屏覆盖
        showBlockingOverlay(packageName);
    }
    
    private void showBlockingOverlay(String packageName) {
        try {
            // 启动悬浮窗服务显示覆盖层
            Intent overlayIntent = new Intent(this, BlockOverlayService.class);
            overlayIntent.putExtra("package_name", packageName);
            startService(overlayIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error showing blocking overlay", e);
        }
    }
}