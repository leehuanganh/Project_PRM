package com.example.project.ui_all.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsYearFragment extends Fragment {
    private BarChart barChart;
    private TransactionDAO transactionDAO;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics_year, container, false);
        barChart = view.findViewById(R.id.barChartYear);
        // Ẩn Toolbar của Activity chứa Fragment này
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
        transactionDAO = new TransactionDAO(getContext());
        calendar = Calendar.getInstance();
        loadStatistics();
        return view;
    }

    private void loadStatistics() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.MONTH, Calendar.JANUARY); // Đặt về tháng 1

        for (int i = 0; i < 12; i++) { // Lấy dữ liệu cho từng tháng trong năm
            String monthLabel = new SimpleDateFormat("MMM", Locale.getDefault()).format(tempCalendar.getTime());
            labels.add(monthLabel);
            float monthlyIncome = transactionDAO.getTotalIncomeForMonth(tempCalendar);
            float monthlyExpense = transactionDAO.getTotalExpenseForMonth(tempCalendar);
            incomeEntries.add(new BarEntry(i, monthlyIncome));
            expenseEntries.add(new BarEntry(i, monthlyExpense));
            tempCalendar.add(Calendar.MONTH, 1);
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
    }
}
