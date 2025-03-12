package com.example.project.ui_all;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private BarChart barChart;
    private ImageButton btnPrev, btnNext, btnDatePicker;
    private TextView tvDate, tvTotalIncome, tvTotalExpense;
    private TransactionDAO transactionDAO;
    private Calendar calendar;
    private String currentFilter = "WEEK"; // Mặc định hiển thị theo tuần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tabLayout = findViewById(R.id.tabLayout);
        barChart = findViewById(R.id.barChart);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        tvDate = findViewById(R.id.tvDate);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        transactionDAO = new TransactionDAO(this);
        calendar = Calendar.getInstance();

        setupTabLayout();
        loadStatistics();

        btnPrev.setOnClickListener(v -> changeDate(-1));
        btnNext.setOnClickListener(v -> changeDate(1));
        btnDatePicker.setOnClickListener(v -> showDatePicker());
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, new ViewPager2(this), (tab, position) -> {
            String[] labels = {"Ngày", "Tuần", "Tháng", "Quý", "Năm"};
            tab.setText(labels[position]);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String[] filters = {"DAY", "WEEK", "MONTH", "QUARTER", "YEAR"};
                currentFilter = filters[tab.getPosition()];
                loadStatistics();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đặt tab mặc định là "Tuần"
        tabLayout.selectTab(tabLayout.getTabAt(1));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    loadStatistics();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void changeDate(int step) {
        switch (currentFilter) {
            case "DAY": calendar.add(Calendar.DAY_OF_MONTH, step); break;
            case "WEEK": calendar.add(Calendar.WEEK_OF_MONTH, step); break;
            case "MONTH": calendar.add(Calendar.MONTH, step); break;
            case "QUARTER": calendar.add(Calendar.MONTH, step * 3); break;
            case "YEAR": calendar.add(Calendar.YEAR, step); break;
        }
        loadStatistics();
    }

    private void loadStatistics() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar tempCalendar = (Calendar) calendar.clone();

        float totalIncome = 0;
        float totalExpense = 0;

        for (int i = 6; i >= 0; i--) {
            tempCalendar.add(Calendar.DAY_OF_MONTH, -i);
            String date = sdf.format(tempCalendar.getTime());
            labels.add(date);

            float dailyIncome = transactionDAO.getTotalIncomeForDate(date);
            float dailyExpense = transactionDAO.getTotalExpenseForDate(date);

            incomeEntries.add(new BarEntry(labels.size() - 1, dailyIncome));
            expenseEntries.add(new BarEntry(labels.size() - 1, dailyExpense));

            totalIncome += dailyIncome;
            totalExpense += dailyExpense;

            tempCalendar.add(Calendar.DAY_OF_MONTH, i);
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Thu nhập");
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Chi tiêu");
        incomeDataSet.setColor(Color.parseColor("#4CAF50"));
        expenseDataSet.setColor(Color.parseColor("#F44336"));

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barChart.setData(barData);
        barChart.invalidate();

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        tvTotalIncome.setText(String.format(Locale.US, "Thu nhập: %.0f VND", totalIncome));
        tvTotalExpense.setText(String.format(Locale.US, "Chi tiêu: %.0f VND", totalExpense));
    }
}
