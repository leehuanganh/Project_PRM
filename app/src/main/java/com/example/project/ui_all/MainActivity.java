package com.example.project.ui_all;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Kiểm tra đăng nhập
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // Chưa đăng nhập → Chuyển đến LoginActivity
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        } else {
            // Đã đăng nhập → Chuyển đến DashboardActivity
            Intent dashboardIntent = new Intent(this, DashboardActivity.class);
            dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(dashboardIntent);
        }

        finish(); // Đóng MainActivity ngay lập tức để tránh quay lại màn hình này
    }
}
