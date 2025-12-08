package com.example.testapp.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

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
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOpsManager != null) {
                int mode;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mode = appOpsManager.unsafeCheckOpNoThrow(
                            "android:ignore_battery_optimizations",
                            android.os.Process.myUid(),
                            context.getPackageName());
                } else {
                    mode = appOpsManager.checkOpNoThrow(
                            "android:ignore_battery_optimizations",
                            android.os.Process.myUid(),
                            context.getPackageName());
                }
                return mode == AppOpsManager.MODE_ALLOWED;
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
    
    // 检查是否有所需权限
    public boolean hasAllPermissions() {
        return hasOverlayPermission() && 
               hasUsageStatsPermission() && 
               hasBatteryOptimizationPermission();
    }
}