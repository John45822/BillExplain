package com.billexplain.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.billexplain.app.R;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityAnalyticsBinding;
import com.billexplain.app.models.Bill;
import com.billexplain.app.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {
    private ActivityAnalyticsBinding binding;
    private List<Bill> bills;
    private boolean isBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        bills = db.getBillsForUser(session.getUserId());
        // Show oldest first for chart
        Collections.reverse(bills);
        if (bills.size() > 6) bills = bills.subList(bills.size() - 6, bills.size());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBar.setOnClickListener(v -> {
            isBar = true;
            binding.btnBar.setSelected(true);
            binding.btnLine.setSelected(false);
            showBarChart();
            binding.barChart.setVisibility(android.view.View.VISIBLE);
            binding.lineChart.setVisibility(android.view.View.GONE);
        });
        binding.btnLine.setOnClickListener(v -> {
            isBar = false;
            binding.btnBar.setSelected(false);
            binding.btnLine.setSelected(true);
            showLineChart();
            binding.barChart.setVisibility(android.view.View.GONE);
            binding.lineChart.setVisibility(android.view.View.VISIBLE);
        });

        showBarChart();
        binding.barChart.setVisibility(android.view.View.VISIBLE);
        binding.lineChart.setVisibility(android.view.View.GONE);

        // Stats
        if (!bills.isEmpty()) {
            float max = 0, min = Float.MAX_VALUE;
            for (Bill b : bills) {
                if (b.totalAmount > max) max = b.totalAmount;
                if (b.totalAmount < min) min = b.totalAmount;
            }
            Bill last = bills.get(bills.size() - 1);
            Bill prev = bills.size() > 1 ? bills.get(bills.size() - 2) : last;
            float pct = prev.totalAmount > 0 ? ((last.totalAmount - prev.totalAmount) / prev.totalAmount) * 100 : 0;

            binding.tvStatHigh.setText(String.format("₱%,.0f", max));
            binding.tvStatLow.setText(String.format("₱%,.0f", min));
            binding.tvStatMoM.setText(String.format("%+.1f%%", pct));
            if (pct > 0) binding.tvStatMoM.setTextColor(ContextCompat.getColor(this, R.color.red));
            else binding.tvStatMoM.setTextColor(ContextCompat.getColor(this, R.color.green));
        }
    }

    private String[] getLabels() {
        String[] labels = new String[bills.size()];
        for (int i = 0; i < bills.size(); i++)
            labels[i] = bills.get(i).month.substring(0, 3);
        return labels;
    }

    private void showBarChart() {
        BarChart chart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < bills.size(); i++)
            entries.add(new BarEntry(i, bills.get(i).totalAmount));

        BarDataSet ds = new BarDataSet(entries, "Bill Amount");
        int[] colors = new int[entries.size()];
        for (int i = 0; i < entries.size() - 1; i++)
            colors[i] = ContextCompat.getColor(this, R.color.blue);
        colors[entries.size() - 1] = ContextCompat.getColor(this, R.color.red);
        ds.setColors(colors);
        ds.setDrawValues(true);
        ds.setValueTextSize(10f);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getBarLabel(BarEntry e) {
                return String.format("₱%.0f", e.getY());
            }
        });

        BarData data = new BarData(ds);
        data.setBarWidth(0.6f);
        chart.setData(data);
        styleChart(chart.getXAxis(), chart.getAxisLeft(), chart.getAxisRight(), getLabels());
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void showLineChart() {
        LineChart chart = binding.lineChart;
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < bills.size(); i++)
            entries.add(new Entry(i, bills.get(i).totalAmount));

        LineDataSet ds = new LineDataSet(entries, "Bill Amount");
        ds.setColor(ContextCompat.getColor(this, R.color.blue));
        ds.setCircleColor(ContextCompat.getColor(this, R.color.blue));
        ds.setCircleRadius(5f);
        ds.setLineWidth(2.5f);
        ds.setDrawFilled(true);
        ds.setFillColor(ContextCompat.getColor(this, R.color.blue_light));
        ds.setFillAlpha(60);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setDrawValues(true);
        ds.setValueTextSize(10f);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getPointLabel(Entry e) {
                return String.format("₱%.0f", e.getY());
            }
        });

        LineData data = new LineData(ds);
        chart.setData(data);
        styleChart(chart.getXAxis(), chart.getAxisLeft(), chart.getAxisRight(), getLabels());
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    private void styleChart(XAxis xAxis, YAxis left, YAxis right, String[] labels) {
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#6B7280"));
        xAxis.setTextSize(11f);
        left.setTextColor(Color.parseColor("#9CA3AF"));
        left.setTextSize(10f);
        left.setAxisMinimum(2000f);
        left.setValueFormatter(new ValueFormatter() {
            @Override public String getAxisLabel(float v, com.github.mikephil.charting.components.AxisBase a) {
                return "₱" + (int)(v / 1000) + "k";
            }
        });
        right.setEnabled(false);
    }
}
