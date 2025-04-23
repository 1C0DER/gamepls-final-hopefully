package uk.ac.rgu.gamepls.track;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;  // Import BarChart
import com.github.mikephil.charting.data.BarEntry;  // Import BarEntry
import com.github.mikephil.charting.data.BarDataSet;  // Import BarDataSet
import com.github.mikephil.charting.data.BarData;  // Import BarData
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    // Method to load the statistics for the past 7 days and update the BarChart
    public void loadStatistics() {
        // You can replace this with actual logic to get daily app usage data
        long[] dailyUsage = new long[7];  // Array to store total usage for each of the 7 days
        dailyUsage[0] = 12000000;  // Example: 12 seconds on Sunday
        dailyUsage[1] = 5000000;   // Example: 5 seconds on Monday
        dailyUsage[2] = 6000000;   // Example: 6 seconds on Tuesday
        dailyUsage[3] = 10000000;  // Example: 10 seconds on Wednesday
        dailyUsage[4] = 8000000;   // Example: 8 seconds on Thursday
        dailyUsage[5] = 7000000;   // Example: 7 seconds on Friday
        dailyUsage[6] = 11000000;  // Example: 11 seconds on Saturday

        updateBarChart(dailyUsage);
    }

    // Update the BarChart with the data for 7 days
    private void updateBarChart(long[] dailyUsage) {
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

        // Create the BarData object and set it on the chart
        BarDataSet barDataSet = new BarDataSet(barEntries, "App Usage");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);  // Set colors for each bar
        barDataSet.setValueTextSize(14f);  // Set the text size for percentage
        barDataSet.setValueTextColor(Color.BLACK);  // Set the text color

        // Create the BarData object with a single dataset
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.invalidate();  // Refresh the chart
    }
}
