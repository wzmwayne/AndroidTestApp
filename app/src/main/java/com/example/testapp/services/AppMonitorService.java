package com.example.testapp.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.testapp.BlockOverlayActivity;
import com.example.testapp.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppMonitorService extends Service {
    private static final String TAG = "AppMonitorService";
    private static final String CHANNEL_ID = "AppMonitorChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL = 1000; // 1秒检查一次

    private Timer timer;
    private Handler handler;
    private String currentForegroundApp;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startMonitoring() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForegroundApp();
            }
        }, 0, CHECK_INTERVAL);
    }

    private void checkForegroundApp() {
        try {
            String foregroundApp = getForegroundApp();
            if (foregroundApp != null && !foregroundApp.equals(currentForegroundApp)) {
                currentForegroundApp = foregroundApp;
                handleAppChange(foregroundApp);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking foreground app", e);
        }
    }

    private String getForegroundApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getForegroundAppLollipop();
        } else {
            return getForegroundAppPreLollipop();
        }
    }

    private String getForegroundAppLollipop() {
        try {
            ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            
            List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : runningApps) {
                if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return process.processName;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting foreground app (Lollipop+)", e);
        }
        return null;
    }

    private String getForegroundAppPreLollipop() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                return tasks.get(0).topActivity.getPackageName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting foreground app (Pre-Lollipop)", e);
        }
        return null;
    }

    private void handleAppChange(String packageName) {
        Log.d(TAG, "Foreground app changed to: " + packageName);

        // 跳过本应用
        if (packageName.equals(getPackageName())) {
            return;
        }

        // 检查保护是否启用
        boolean protectionEnabled = prefs.getBoolean("protection_enabled", true);
        if (!protectionEnabled) {
            return;
        }

        // 获取当前模式
        boolean isBlacklistMode = prefs.getBoolean("is_blacklist_mode", true);
        String appsKey = isBlacklistMode ? "blacklist_apps" : "whitelist_apps";
        String appsString = prefs.getString(appsKey, "");

        if (appsString.isEmpty()) {
            return;
        }

        String[] apps = appsString.split(",");
        boolean shouldBlock = false;

        for (String app : apps) {
            if (app.trim().equals(packageName)) {
                shouldBlock = isBlacklistMode; // 黑名单模式：匹配则拦截，白名单模式：匹配则允许
                break;
            }
        }

        // 白名单模式：未匹配的应用也需要拦截
        if (!isBlacklistMode) {
            shouldBlock = true;
            for (String app : apps) {
                if (app.trim().equals(packageName)) {
                    shouldBlock = false; // 白名单中有，允许使用
                    break;
                }
            }
        }

        if (shouldBlock) {
            Log.d(TAG, "Blocking app: " + packageName);
            blockApp(packageName);
        }
    }

    private void blockApp(String packageName) {
        handler.post(() -> {
            // 显示拦截界面
            Intent intent = new Intent(this, BlockOverlayActivity.class);
            intent.putExtra("title", getString(R.string.app_blocked));
            intent.putExtra("message", "应用 " + packageName + " " + getString(R.string.app_blocked_message));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            // 通知无障碍服务返回桌面
            Intent serviceIntent = new Intent(this, AppBlockAccessibilityService.class);
            serviceIntent.putExtra("action", "block_app");
            serviceIntent.putExtra("package_name", packageName);
            startService(serviceIntent);
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "应用监控服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("监控前台应用并执行拦截");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("应用管理器")
                .setContentText("正在监控前台应用")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}