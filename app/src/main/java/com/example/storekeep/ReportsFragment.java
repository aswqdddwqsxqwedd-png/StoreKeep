package com.example.storekeep;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.util.MoneyFormat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) return;

        int onSurface = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorOnSurface, Color.LTGRAY);
        int surface = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorSurface, Color.DKGRAY);
        double inventorySom = db.getInventoryValueSom();
        double revenueSom = db.getSoldRevenueSom();
        int sold = db.getTotalSoldUnits();
        int stock = db.getTotalStockUnits();

        TextView textInvTotal = root.findViewById(R.id.text_inventory_total_som);
        TextView textSalesRev = root.findViewById(R.id.text_sales_revenue_som);
        textInvTotal.setText(MoneyFormat.som(requireContext(), inventorySom));
        textSalesRev.setText(getString(R.string.report_sales_revenue)
                + ": " + MoneyFormat.som(requireContext(), revenueSom));

        TextView soldStat = root.findViewById(R.id.text_sold_stat);
        TextView stockStat = root.findViewById(R.id.text_stock_stat);
        soldStat.setTextColor(onSurface);
        stockStat.setTextColor(onSurface);
        soldStat.setText(getString(R.string.report_sold) + ": " + sold);
        stockStat.setText(getString(R.string.report_stock) + ": " + stock);

        TextView chartUnitsTitle = root.findViewById(R.id.text_chart_units_title);
        TextView chartMoneyTitle = root.findViewById(R.id.text_chart_money_title);
        chartUnitsTitle.setTextColor(onSurface);
        chartMoneyTitle.setTextColor(onSurface);

        int cSold = ContextCompat.getColor(requireContext(), R.color.chart_sold);
        int cStock = ContextCompat.getColor(requireContext(), R.color.chart_stock);
        int cMoneyInv = ContextCompat.getColor(requireContext(), R.color.chart_stock);
        int cMoneySale = ContextCompat.getColor(requireContext(), R.color.chart_sold);

        setupPieUnits(root.findViewById(R.id.pie_chart_units), sold, stock,
                cSold, cStock, onSurface, surface);

        setupBarMoney(root.findViewById(R.id.bar_chart_money), inventorySom, revenueSom,
                cMoneyInv, cMoneySale, onSurface, surface);
    }

    private void setupPieUnits(PieChart chart, int sold, int stock,
                               int cSold, int cStock, int onSurface, int surface) {
        chart.setHoleColor(surface);
        chart.setTransparentCircleColor(surface);
        chart.setTransparentCircleAlpha(110);

        float total = sold + stock;
        if (total <= 0) {
            chart.setData(null);
            chart.setNoDataText(getString(R.string.chart_by_quantity));
            chart.setNoDataTextColor(onSurface);
            chart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(sold, getString(R.string.report_sold)));
        entries.add(new PieEntry(stock, getString(R.string.report_stock)));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(cSold, cStock);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(onSurface);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(chart));
        chart.setUsePercentValues(true);
        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setEntryLabelColor(onSurface);
        chart.setHoleRadius(42f);
        chart.setTransparentCircleRadius(48f);
        chart.setExtraOffsets(8f, 8f, 8f, 8f);

        Legend leg = chart.getLegend();
        leg.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        leg.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        leg.setTextColor(onSurface);
        leg.setTextSize(12f);

        chart.invalidate();
    }

    private void setupBarMoney(BarChart chart, double inventorySom, double revenueSom,
                               int cInv, int cSale, int onSurface, int surface) {
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setExtraOffsets(8f, 16f, 8f, 8f);
        chart.setNoDataText(getString(R.string.chart_by_money));
        chart.setNoDataTextColor(onSurface);
        chart.setBackgroundColor(Color.TRANSPARENT);

        float inv = (float) inventorySom;
        float rev = (float) revenueSom;
        if (inv <= 0 && rev <= 0) {
            chart.setData(null);
            chart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, inv));
        entries.add(new BarEntry(1f, rev));

        BarDataSet set = new BarDataSet(entries, "");
        set.setColors(cInv, cSale);
        set.setValueTextColor(onSurface);
        set.setValueTextSize(11f);
        Context ctx = getContext();
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (ctx == null) return "";
                return MoneyFormat.som(ctx, value);
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.45f);
        chart.setData(data);
        chart.setFitBars(true);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(onSurface);
        x.setValueFormatter(new IndexAxisValueFormatter(new String[]{
                getString(R.string.chart_bar_label_inventory),
                getString(R.string.chart_bar_label_sales)
        }));

        chart.getAxisLeft().setTextColor(onSurface);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(ColorUtils.setAlphaComponent(onSurface, 48));
        chart.getAxisLeft().setValueFormatter(new AxisMoneyCompactFormatter());
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.invalidate();
    }

    /** Короткий формат чисел на оси Y столбчатой диаграммы. */
    private static final class AxisMoneyCompactFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (value <= 0) return "0";
            if (value >= 1_000_000f) {
                return String.format(Locale.US, "%.1fм", value / 1_000_000f);
            }
            if (value >= 1000f) {
                return String.format(Locale.US, "%.0fт", value / 1000f);
            }
            return String.format(Locale.US, "%.0f", value);
        }
    }
}
