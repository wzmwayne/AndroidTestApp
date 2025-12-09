package com.example.testapp.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

public class DebugClickDetector {
    private static final int CLICK_COUNT = 5;
    private static final long CLICK_TIMEOUT = 2000; // 2秒内完成5次点击
    
    private static DebugClickDetector instance;
    private int clickCount = 0;
    private long lastClickTime = 0;
    private Handler handler;
    private Runnable resetRunnable;
    private DebugOverlayManager debugOverlayManager;
    
    private DebugClickDetector() {
        handler = new Handler(Looper.getMainLooper());
        resetRunnable = this::resetClickCount;
    }
    
    public static synchronized DebugClickDetector getInstance() {
        if (instance == null) {
            instance = new DebugClickDetector();
        }
        return instance;
    }
    
    public void setupDebugDetection(Activity activity, View rootView) {
        if (debugOverlayManager == null) {
            debugOverlayManager = DebugOverlayManager.getInstance(activity);
        }
        
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handleTouchEvent(event.getX(), event.getY(), v.getWidth(), v.getHeight());
            }
            return false;
        });
    }
    
    private void handleTouchEvent(float x, float y, int viewWidth, int viewHeight) {
        // 检查是否在左上角区域（左上角1/4区域）
        boolean isTopLeftCorner = x <= viewWidth / 4 && y <= viewHeight / 4;
        
        if (isTopLeftCorner) {
            long currentTime = System.currentTimeMillis();
            
            // 如果距离上次点击超过超时时间，重置计数
            if (currentTime - lastClickTime > CLICK_TIMEOUT) {
                resetClickCount();
            }
            
            lastClickTime = currentTime;
            clickCount++;
            
            // 移除之前的重置任务
            handler.removeCallbacks(resetRunnable);
            
            // 设置新的重置任务
            handler.postDelayed(resetRunnable, CLICK_TIMEOUT);
            
            // 如果达到点击次数，显示调试悬浮窗
            if (clickCount >= CLICK_COUNT) {
                showDebugOverlay();
                resetClickCount();
            }
        }
    }
    
    private void showDebugOverlay() {
        if (debugOverlayManager != null) {
            debugOverlayManager.showDebugOverlay();
        }
    }
    
    private void resetClickCount() {
        clickCount = 0;
        handler.removeCallbacks(resetRunnable);
    }
    
    public void showErrorDebug(Activity activity, String errorMessage) {
        if (debugOverlayManager == null) {
            debugOverlayManager = DebugOverlayManager.getInstance(activity);
        }
        
        debugOverlayManager.updateDebugInfo("错误信息: " + errorMessage);
        debugOverlayManager.showDebugOverlay();
    }
}