package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class BlockOverlayActivity extends Activity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final long AUTO_DISMISS_DELAY = 3000; // 3秒后自动关闭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 设置为全屏覆盖
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.activity_block_overlay);

        setupUI();
        
        // 自动关闭
        handler.postDelayed(this::finish, AUTO_DISMISS_DELAY);
    }

    private void setupUI() {
        TextView titleText = findViewById(R.id.titleText);
        TextView messageText = findViewById(R.id.messageText);
        Button okButton = findViewById(R.id.okButton);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            
            if (title != null) {
                titleText.setText(title);
            }
            if (message != null) {
                messageText.setText(message);
            }
        }

        okButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        // 阻止返回键
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}