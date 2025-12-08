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
        // 这里可以添加额外的窗口状态检测逻辑
        // 主要的拦截逻辑在AppMonitorService中处理
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
        Log.d(TAG, "Performing block action for: " + packageName);
        
        // 返回主屏幕
        performGlobalAction(GLOBAL_ACTION_HOME);
        
        // 可以添加其他拦截操作，如显示Toast等
        // 但注意在无障碍服务中直接显示UI可能有限制
    }
}