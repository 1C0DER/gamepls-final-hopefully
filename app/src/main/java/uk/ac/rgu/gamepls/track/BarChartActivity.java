package uk.ac.rgu.gamepls.track;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.view.View;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import uk.ac.rgu.gamepls.MainActivity;
import uk.ac.rgu.gamepls.R;

public class BarChartActivity extends AppCompatActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        barChart = findViewById(R.id.barChart);  // Initialize BarChart

        // Load data and update the chart
        loadStatistics();
    }

    public void onBackArrowClick(View view) {
        //navigate back to MainActivity
        Intent intent = new Intent(BarChartActivity.this, MainActivity.class);
        startActivity(intent); // Start the MainActivity
        finish();
    }

    // Method to load the statistics for the past 7 days and update the BarChart
    public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        // Get the current date
        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.DAYS.toMillis(7); // Go back 7 full days

        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        // List to hold the daily total usage time for the last 7 days
        long[] dailyUsage = new long[7];

        // Calendar instance to help with day comparison
        Calendar calendar = Calendar.getInstance();
        Calendar usageCalendar = Calendar.getInstance();

        // Iterate through the usage stats and aggregate by day
        for (UsageStats usageStats : appList) {
            usageCalendar.setTimeInMillis(usageStats.getLastTimeUsed());

            // Iterate through the 7 days we are tracking
            for (int i = 0; i < 7; i++) {
                calendar.setTimeInMillis(endTime - TimeUnit.DAYS.toMillis(6 - i)); // Get the timestamp for each of the last 7 days

                if (usageCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        usageCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        usageCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    dailyUsage[i] += usageStats.getTotalTimeInForeground();
                    break;
                }
            }
        }

        // Update the BarChart with the new data
        updateBarChart(dailyUsage);
    }

    // Update the BarChart with the data for 7 days
    private void updateBarChart(long[] dailyUsage) {

        int[] colors = {
                Color.parseColor("#1E90FF"),  // Dodger Blue (light blue)
                Color.parseColor("#4682B4"),  // Steel Blue (darker blue)
                Color.parseColor("#5F9EA0"),  // Cadet Blue (teal-ish)
                Color.parseColor("#ADD8E6"),  // Light Blue
                Color.parseColor("#00BFFF"),  // Deep Sky Blue (vibrant)
                Color.parseColor("#87CEEB"),  // Sky Blue
                Color.parseColor("#B0C4DE")   // Light Steel Blue (faded blue)
        };

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // Prepare the data for each of the last 7 days
        for (int i = 0; i < 7; i++) {
            float usageInHours = dailyUsage[i] / (3600000f);  // Convert milliseconds to hours
            usageInHours = Math.min(usageInHours, 24f);      // Cap usage at 24 hours
            barEntries.add(new BarEntry(i, usageInHours));
        }

        // Axis Configuration
        barChart.getAxisLeft().setAxisMinimum(0f);  // Set minimum value of Y-Axis to 0
        barChart.getAxisLeft().setAxisMaximum(24f);  // Maximum value is 24 hours
        barChart.getAxisRight().setEnabled(false);  // Disable the right Y-Axis

        // Set Description (Optional)
        Description description = new Description();
        description.setText("Usage in Hours per Day (Last 7 Days)");
        barChart.setDescription(description);

        // Set Legend
        Legend legend = barChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setTextSize(12f);

        // X-Axis Display Days of the Week
        final String[] daysOfWeek = new String[7];
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            calendar.setTimeInMillis(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6 - i));
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    daysOfWeek[i] = "Sun";
                    break;
                case Calendar.MONDAY:
                    daysOfWeek[i] = "Mon";
                    break;
                case Calendar.TUESDAY:
                    daysOfWeek[i] = "Tue";
                    break;
                case Calendar.WEDNESDAY:
                    daysOfWeek[i] = "Wed";
                    break;
                case Calendar.THURSDAY:
                    daysOfWeek[i] = "Thu";
                    break;
                case Calendar.FRIDAY:
                    daysOfWeek[i] = "Fri";
                    break;
                case Calendar.SATURDAY:
                    daysOfWeek[i] = "Sat";
                    break;
            }
        }

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < daysOfWeek.length) {
                    return daysOfWeek[index];
                }
                return "";
            }
        });

        // Y-Axis Formatter: Display Hours (0 - 24)
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + " h";
            }
        });

        // Dataset for the bar chart
        BarDataSet barDataSet = new BarDataSet(barEntries, "App Usage");
        barDataSet.setColors(colors);
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTextColor(Color.BLACK);

        // Create the BarData object and set it on the chart
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Refresh the chart
        barChart.invalidate();
    }
}