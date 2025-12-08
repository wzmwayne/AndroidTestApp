package com.example.testapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testapp.BlockOverlayActivity;
import com.example.testapp.services.AppBlockAccessibilityService;

public class PackageChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;
        
        if (packageName == null) {
            return;
        }

        Log.d(TAG, "Package change detected: " + action + ", package: " + packageName);

        // 检测应用安装
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            handlePackageAdded(context, packageName);
        }
        // 检测应用卸载
        else if (Intent.ACTION_PACKAGE_REMOVED.equals(action) && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
            handlePackageRemoved(context, packageName);
        }
        // 检测应用替换
        else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            handlePackageReplaced(context, packageName);
        }
    }

    private void handlePackageAdded(Context context, String packageName) {
        Log.d(TAG, "New package installed: " + packageName);
        
        // 检查是否是应用管理器本身
        if (packageName.equals(context.getPackageName())) {
            return;
        }

        // 如果是设置、应用商店或系统相关应用，可能需要拦截
        if (isSystemManagementApp(packageName)) {
            blockInstallation(context, packageName);
        }
    }

    private void handlePackageRemoved(Context context, String packageName) {
        Log.d(TAG, "Package removed: " + packageName);
        
        // 如果尝试卸载应用管理器，立即阻止
        if (packageName.equals(context.getPackageName())) {
            preventUninstallation(context);
        }
    }

    private void handlePackageReplaced(Context context, String packageName) {
        Log.d(TAG, "Package replaced: " + packageName);
        
        // 检查是否是应用管理器被更新
        if (packageName.equals(context.getPackageName())) {
            // 重新启动服务确保功能正常
            restartServices(context);
        }
    }

    private boolean isSystemManagementApp(String packageName) {
        // 系统管理相关的包名
        String[] managementApps = {
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.huawei.appmarket",
            "com.oppo.market",
            "com.vivo.appstore",
            "com.xiaomi.market",
            "com.sec.android.app.myfiles",
            "com.sec.android.app.launcher",
            "com.samsung.android.app.galaxyfinder"
        };

        for (String app : managementApps) {
            if (packageName.contains(app)) {
                return true;
            }
        }
        return false;
    }

    private void blockInstallation(Context context, String packageName) {
        Log.d(TAG, "Blocking installation of: " + packageName);
        
        // 显示拦截界面
        showBlockOverlay(context, "应用安装已被阻止", packageName);
    }

    private void preventUninstallation(Context context) {
        Log.d(TAG, "Preventing uninstallation of app manager");
        
        // 显示拦截界面
        showBlockOverlay(context, "应用卸载已被阻止", "本应用受保护，无法卸载");
    }

    private void showBlockOverlay(Context context, String title, String message) {
        Intent intent = new Intent(context, BlockOverlayActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void restartServices(Context context) {
        // 重启无障碍服务
        Intent intent = new Intent(context, AppBlockAccessibilityService.class);
        context.startService(intent);
    }
}