package com.example.project.ui_all;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationDashboard);

        // 🔹 Nếu vào lần đầu, chuyển đến `StatisticsActivity` (Hiển thị tab Tuần)
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
            startActivity(new Intent(this, StatisticsActivity.class));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(this, StatisticsActivity.class));
                    return true;

                case R.id.nav_statistics:
                    startActivity(new Intent(this, TransactionListActivity.class));
                    return true;

                case R.id.nav_add_transaction:
                    startActivity(new Intent(this, TransactionActivity.class));
                    return true;

                case R.id.nav_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;

                default:
                    return false;
            }
        });
    }
}
