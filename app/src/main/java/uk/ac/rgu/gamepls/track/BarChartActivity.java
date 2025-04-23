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
import uk.ac.rgu.gamepls.MainActivity;
import uk.ac.rgu.gamepls.R;

public class BarChartActivity extends AppCompatActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        barChart = findViewById(R.id.barChart);  // Initialize BarChart

        // You would call your method to load data and update the chart
        loadStatistics();
    }

    // This method will be called when the ImageButton is clicked
    public void onBackArrowClick(View view) {
        // Create an Intent to navigate back to MainActivity
        Intent intent = new Intent(BarChartActivity.this, MainActivity.class);
        startActivity(intent); // Start the MainActivity
        finish(); // Optionally finish the current activity if you want to remove it from the back stack
    }

    // Method to load the statistics for the past 7 days and update the BarChart
    public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        long endTime = System.currentTimeMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -6);  // Start from 7 days ago
        long startTime = calendar.getTimeInMillis();

        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        // Create a list to hold the daily total usage time for 7 days
        long[] dailyUsage = new long[7];  // Array to store total usage for each of the 7 days

        // Fill dailyUsage with the total time used for each day
        for (UsageStats usageStats : appList) {
            int dayOfWeek = getDayOfWeek(usageStats.getLastTimeUsed());
            long totalTime = usageStats.getTotalTimeInForeground();
            dailyUsage[dayOfWeek] += totalTime;  // Update the respective day with the usage time
        }

        // Update the BarChart with the new data
        updateBarChart(dailyUsage);
    }

    // Get the day of the week (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
    private int getDayOfWeek(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;  // Adjust for 0-based index (Sunday = 0, Saturday = 6)
    }

    // Update the BarChart with the data for 7 days
    private void updateBarChart(long[] dailyUsage) {

        int[] colors = {
                Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GRAY
        };

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // Prepare the data for each day of the week (Sunday to Saturday)
        for (int i = 0; i < 7; i++) {
            float usageInHours = dailyUsage[i] / (3600000f);  // Convert milliseconds to hours
            usageInHours = Math.min(usageInHours, 24f);  // Cap usage at 24 hours
            barEntries.add(new BarEntry(i, usageInHours));
        }

        // Axis Configuration
        barChart.getAxisLeft().setAxisMinimum(0f);  // Set minimum value of Y-Axis to 0 (Start of the day)
        barChart.getAxisLeft().setAxisMaximum(24f);  // Maximum value is 24 hours
        barChart.getAxisRight().setEnabled(false);  // Optionally, disable the right Y-Axis if unnecessary

        // Set Description (Optional)
        Description description = new Description();
        description.setText("Usage in Hours per Day");
        barChart.setDescription(description);

        // Set Legend
        Legend legend = barChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setTextSize(12f);

        // X-Axis Formatter: Display Days of the Week
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;  // Explicit cast from float to int
                return daysOfWeek[index % daysOfWeek.length];  // Wrap around the index if it exceeds the days of the week
            }
        });

        // Y-Axis Formatter: Display Hours (0 - 24)
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int hours = (int) value;  // Cast float to int
                return hours + " h";  // Return as string (e.g., "3 h")
            }
        });

        // Create a dataset for the bar chart
        BarDataSet barDataSet = new BarDataSet(barEntries, "App Usage");
        barDataSet.setColors(colors);  // Assign the different colors to the bars
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTextColor(Color.BLACK);

        // Create the BarData object and set it on the chart
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Refresh the chart
        barChart.invalidate();
    }
}
