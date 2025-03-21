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

public class StatisticsDayFragment extends Fragment {
    private BarChart barChart;
    private TransactionDAO transactionDAO;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics_day, container, false);
        barChart = view.findViewById(R.id.barChart);
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
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar tempCalendar = (Calendar) calendar.clone();

        for (int i = 6; i >= 0; i--) {
            tempCalendar.add(Calendar.DAY_OF_MONTH, -i);
            String date = sdf.format(tempCalendar.getTime());
            labels.add(date);
            float dailyExpense = transactionDAO.getTotalExpenseForDate(date);
            entries.add(new BarEntry(labels.size() - 1, dailyExpense));
            tempCalendar.add(Calendar.DAY_OF_MONTH, i);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColor(Color.parseColor("#F44336"));
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }
}


