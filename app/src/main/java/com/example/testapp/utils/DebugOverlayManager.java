package com.example.testapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.R;

public class DebugOverlayManager {
    private static DebugOverlayManager instance;
    private Context context;
    private WindowManager windowManager;
    private View debugOverlay;
    private boolean isShowing = false;
    
    private DebugOverlayManager(Context context) {
        this.context = context.getApplicationContext();
        windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
    }
    
    public static synchronized DebugOverlayManager getInstance(Context context) {
        if (instance == null) {
            instance = new DebugOverlayManager(context);
        }
        return instance;
    }
    
    public void showDebugOverlay() {
        if (isShowing) {
            return;
        }
        
        // 检查悬浮窗权限
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }
        
        try {
            // 创建悬浮窗视图
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            debugOverlay = inflater.inflate(R.layout.debug_overlay, null);
            
            // 设置窗口参数
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;
            
            // 添加到窗口管理器
            windowManager.addView(debugOverlay, params);
            
            // 设置触摸事件
            setupTouchEvents(params);
            
            // 设置按钮事件
            setupButtonEvents();
            
            isShowing = true;
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "显示调试窗口失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    public void hideDebugOverlay() {
        if (!isShowing || debugOverlay == null) {
            return;
        }
        
        try {
            windowManager.removeView(debugOverlay);
            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupTouchEvents(WindowManager.LayoutParams params) {
        debugOverlay.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(debugOverlay, params);
                        return true;
                }
                return false;
            }
        });
    }
    
    private void setupButtonEvents() {
        // 关闭按钮
        Button closeButton = debugOverlay.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(v -> hideDebugOverlay());
        
        // 重置密码按钮
        Button resetPasswordButton = debugOverlay.findViewById(R.id.btnResetPassword);
        resetPasswordButton.setOnClickListener(v -> {
            new PasswordManager(context).setFirstTimeComplete();
            Toast.makeText(context, "已重置首次使用状态", Toast.LENGTH_SHORT).show();
        });
        
        // 查看日志按钮
        Button viewLogsButton = debugOverlay.findViewById(R.id.btnViewLogs);
        viewLogsButton.setOnClickListener(v -> {
            // 这里可以添加查看日志的功能
            Toast.makeText(context, "日志功能暂未实现", Toast.LENGTH_SHORT).show();
        });
        
        // 清除数据按钮
        Button clearDataButton = debugOverlay.findViewById(R.id.btnClearData);
        clearDataButton.setOnClickListener(v -> {
            try {
                // 清除应用数据
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                Toast.makeText(context, "已清除所有数据", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "清除数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Toast.makeText(context, "请授予悬浮窗权限以显示调试窗口", Toast.LENGTH_LONG).show();
        }
    }
    
    public void updateDebugInfo(String info) {
        if (!isShowing || debugOverlay == null) {
            return;
        }
        
        TextView debugText = debugOverlay.findViewById(R.id.debugText);
        if (debugText != null) {
            debugText.setText(info);
        }
    }
}