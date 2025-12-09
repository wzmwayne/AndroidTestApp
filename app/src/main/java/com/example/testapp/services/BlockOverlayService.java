package com.example.testapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class BlockOverlayService extends Service {
    private static final String TAG = "BlockOverlayService";
    
    private WindowManager windowManager;
    private View overlayView;
    private String blockedPackageName;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            blockedPackageName = intent.getStringExtra("package_name");
            showOverlay();
        }
        return START_STICKY;
    }

    private void showOverlay() {
        try {
            // 创建覆盖层布局参数
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

            // 创建覆盖层视图
            overlayView = LayoutInflater.from(this).inflate(android.R.layout.activity_list_item, null);
            
            // 设置黑色背景
            overlayView.setBackgroundColor(0xFF000000);
            
            // 创建内容布局
            android.widget.LinearLayout contentLayout = new android.widget.LinearLayout(this);
            contentLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            contentLayout.setGravity(Gravity.CENTER);
            contentLayout.setPadding(50, 50, 50, 50);
            
            // 添加提示文本
            TextView messageText = new TextView(this);
            messageText.setText("应用已被拦截");
            messageText.setTextSize(24);
            messageText.setTextColor(0xFFFFFFFF);
            messageText.setGravity(Gravity.CENTER);
            contentLayout.addView(messageText);
            
            // 添加应用名称
            TextView appText = new TextView(this);
            try {
                String appName = getPackageManager().getApplicationInfo(blockedPackageName, 0).loadLabel(getPackageManager()).toString();
                appText.setText(appName);
            } catch (Exception e) {
                appText.setText(blockedPackageName);
            }
            appText.setTextSize(18);
            appText.setTextColor(0xFFCCCCCC);
            appText.setGravity(Gravity.CENTER);
            appText.setPadding(0, 20, 0, 30);
            contentLayout.addView(appText);
            
            // 添加关闭按钮
            Button closeButton = new Button(this);
            closeButton.setText("确定");
            closeButton.setOnClickListener(v -> hideOverlay());
            contentLayout.addView(closeButton);
            
            // 将内容布局添加到覆盖层
            android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(this);
            frameLayout.addView(contentLayout);
            
            overlayView = frameLayout;
            
            // 显示覆盖层
            windowManager.addView(overlayView, params);
            
            Log.d(TAG, "Overlay shown for package: " + blockedPackageName);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing overlay", e);
            stopSelf();
        }
    }

    private void hideOverlay() {
        try {
            if (overlayView != null && windowManager != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error hiding overlay", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideOverlay();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}