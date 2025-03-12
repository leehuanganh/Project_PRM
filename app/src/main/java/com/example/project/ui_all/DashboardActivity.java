package com.example.project.ui_all;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Chỉ mở `StatisticsActivity` khi Dashboard được hiển thị lần đầu
        if (savedInstanceState == null) {
            startActivity(new Intent(this, StatisticsActivity.class));
        }

        // Xử lý BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationDashboard);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, TransactionListActivity.class));
                return true;
            } else if (itemId == R.id.nav_statistics) {
                return true;  // Đã ở màn hình thống kê, không cần làm gì
            } else if (itemId == R.id.nav_add_transaction) {
                startActivity(new Intent(this, TransactionActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            return false;
        });
    }
}
