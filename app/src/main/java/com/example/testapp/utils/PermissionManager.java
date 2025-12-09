package com.example.testapp.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

public class PermissionManager {
    private Context context;
    
    public PermissionManager(Context context) {
        this.context = context;
    }
    
    // 检查是否有悬浮窗权限
    public boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    // 请求悬浮窗权限
    public Intent getOverlayPermissionIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
        }
        return null;
    }
    
    // 检查是否有使用情况访问权限
    public boolean hasUsageStatsPermission() {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOpsManager == null) {
            return false;
        }
        
        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName());
        } else {
            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.getPackageName());
        }
        
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    
    // 请求使用情况访问权限
    public Intent getUsageStatsPermissionIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }
    
    // 检查是否有电池优化豁免权限
    public boolean hasBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsManager != null) {
                    int mode;
                    // 使用字符串常量而不是AppOpsManager常量，确保兼容性
                    String opString = "android:ignore_battery_optimizations";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        mode = appOpsManager.unsafeCheckOpNoThrow(
                                opString,
                                android.os.Process.myUid(),
                                context.getPackageName());
                    } else {
                        mode = appOpsManager.checkOpNoThrow(
                                opString,
                                android.os.Process.myUid(),
                                context.getPackageName());
                    }
                    return mode == AppOpsManager.MODE_ALLOWED;
                }
            } catch (Exception e) {
                // 如果检查失败，默认返回true
                return true;
            }
        }
        return true;
    }
    
    // 请求电池优化豁免权限
    public Intent getBatteryOptimizationPermissionIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            return intent;
        }
        return null;
    }
    
    // 检查是否有无障碍服务权限
    public boolean hasAccessibilityPermission() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        if (accessibilityEnabled == 1) {
            String service = context.getPackageName() + "/" + 
                           com.example.testapp.services.AppBlockAccessibilityService.class.getCanonicalName();
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessibilityService = splitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // 请求无障碍服务权限
    public Intent getAccessibilityPermissionIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }
    
    // 检查是否有所需权限
    public boolean hasAllPermissions() {
        return hasOverlayPermission() && 
               hasUsageStatsPermission() && 
               hasBatteryOptimizationPermission() &&
               hasAccessibilityPermission();
    }
}