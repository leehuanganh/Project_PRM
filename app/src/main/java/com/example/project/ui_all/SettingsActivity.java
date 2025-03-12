package com.example.project.ui_all;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.auth.LoginActivity;
import com.example.project.auth.ChangePasswordActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchSync, switchNotifications;
    private Button btnChangePassword, btnLogout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ UI
        switchSync = findViewById(R.id.switchSync);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        sharedPreferences = getSharedPreferences("user_settings", MODE_PRIVATE);

        // Load trạng thái cài đặt
        loadSettings();

        // Xử lý bật/tắt đồng bộ dữ liệu
        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("sync_enabled", isChecked);
            Toast.makeText(this, "Đồng bộ: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
        });

        // Xử lý bật/tắt thông báo
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notifications_enabled", isChecked);
            Toast.makeText(this, "Thông báo: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
        });

        // Xử lý đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class));
        });

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadSettings() {
        boolean isSyncEnabled = sharedPreferences.getBoolean("sync_enabled", false);
        boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        switchSync.setChecked(isSyncEnabled);
        switchNotifications.setChecked(isNotificationsEnabled);
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
