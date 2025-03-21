package com.example.project.ui_all;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.example.project.R;
import com.example.project.adapter.StatisticsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton btnPrev, btnNext, btnDatePicker;
    private TextView tvDate;
    private Calendar calendar;
    private String currentFilter = "WEEK"; // Mặc định hiển thị theo tuần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        tvDate = findViewById(R.id.tvDate);

        calendar = Calendar.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);


        // Gán tiêu đề tùy chỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thống kê chi tiêu"); // ✅ Tiêu đề của từng màn hình
        }

        setupViewPager();
        setupTabLayout();
        updateDateText();

        btnPrev.setOnClickListener(v -> changeDate(-1));
        btnNext.setOnClickListener(v -> changeDate(1));
        btnDatePicker.setOnClickListener(v -> showDatePicker());

        // 🔹 Nhận tab mặc định từ DashboardActivity (nếu có)
        int defaultTab = getIntent().getIntExtra("default_tab", 1);
        viewPager.setCurrentItem(defaultTab, false);
    }

    private void setupViewPager() {
        StatisticsPagerAdapter adapter = new StatisticsPagerAdapter(this);
        viewPager.setAdapter(adapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String[] labels = {"Ngày", "Tuần", "Tháng", "Năm"};
            tab.setText(labels[position]);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                String[] filters = {"DAY", "WEEK", "MONTH", "YEAR"};
                currentFilter = filters[tab.getPosition()];
                updateDateText();
            }

            @Override public void onTabUnselected(@NonNull TabLayout.Tab tab) {}
            @Override public void onTabReselected(@NonNull TabLayout.Tab tab) {}
        });

        // 🔹 Đặt tab mặc định là "Tuần"
        if (tabLayout.getTabAt(1) != null) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void changeDate(int step) {
        switch (currentFilter) {
            case "DAY":
                calendar.add(Calendar.DAY_OF_MONTH, step);
                break;
            case "WEEK":
                calendar.add(Calendar.WEEK_OF_YEAR, step);
                break;
            case "MONTH":
                calendar.add(Calendar.MONTH, step);
                break;
//            case "QUARTER":
//                calendar.add(Calendar.MONTH, step * 3);
//                break;
            case "YEAR":
                calendar.add(Calendar.YEAR, step);
                break;
        }
        updateDateText();
    }

    private void updateDateText() {
        SimpleDateFormat sdf;
        switch (currentFilter) {
            case "DAY":
                sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                break;
            case "WEEK":
                sdf = new SimpleDateFormat("'Tuần' w, yyyy", Locale.getDefault());
                break;
            case "MONTH":
                sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                break;
//            case "QUARTER":
//                sdf = new SimpleDateFormat("'Quý' Q, yyyy", Locale.getDefault());
//                break;
            case "YEAR":
                sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
                break;
            default:
                sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }
        tvDate.setText(sdf.format(calendar.getTime()));
    }
}
