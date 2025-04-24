package uk.ac.rgu.gamepls;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

import uk.ac.rgu.gamepls.User.ProfileActivity;
import uk.ac.rgu.gamepls.track.App;
import uk.ac.rgu.gamepls.track.AppsAdapter;
import uk.ac.rgu.gamepls.track.BarChartActivity;

public class MainActivity extends AppCompatActivity {

    Button enableBtn, profileBtn, showBtn, indexHome_btn, viewBarChartBtn;
    TextView permissionDescriptionTv, usageTv;
    ListView appsList;

    private static final String TAG = "AppUsageTracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBtn = findViewById(R.id.enable_btn);
        profileBtn = findViewById(R.id.profile_btn);
        showBtn = findViewById(R.id.show_btn);
        indexHome_btn = findViewById(R.id.indexHome_btn);
        viewBarChartBtn = findViewById(R.id.viewBarChartBtn);
        permissionDescriptionTv = findViewById(R.id.permission_description_tv);
        usageTv = findViewById(R.id.usage_tv);
        appsList = findViewById(R.id.apps_list);

        // On show button click, load the statistics, log them, and update the UI
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadStatisticsAndDisplay();
            }
        });

        // Pass the data to ProfileActivity
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name", "No Name");
                String email = sharedPreferences.getString("email", "No Email");
                String username = sharedPreferences.getString("username", "No Username");

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        indexHome_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set onClickListener to navigate to BarChartActivity
        viewBarChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start BarChartActivity when button is clicked
                Intent intent = new Intent(MainActivity.this, BarChartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getGrantStatus()) {
            showHideWithPermission();
            showBtn.setOnClickListener(view -> loadStatisticsAndDisplay());
        } else {
            showHideNoPermission();
            enableBtn.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        }
    }

    // Store user data in SharedPreferences
    public void storeUserData(String name, String email, String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("username", username);
        editor.apply();
    }

    // Check if PACKAGE_USAGE_STATS permission is granted
    private boolean getGrantStatus() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return (mode == MODE_ALLOWED);
    }

    // Load the statistics of apps usage for the last 24 hours, log them, and update the UI
    public void loadStatisticsAndDisplay() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        // Query usage stats for the past 24 hours
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 1000 * 3600 * 24, System.currentTimeMillis());

        appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());

        if (!appList.isEmpty()) {
            Map<String, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                sortedMap.put(usageStats.getPackageName(), usageStats);
            }
            logAppsUsage(sortedMap);
            showAppsUsage(sortedMap);
        } else {
            Log.i(TAG, "No app usage data found for the last 24 hours.");
            // Optionally clear the list view if there's no data
            AppsAdapter adapter = new AppsAdapter(this, new ArrayList<>());
            appsList.setAdapter(adapter);
        }
    }

    // Log the apps usage stats
    public void logAppsUsage(Map<String, UsageStats> sortedMap) {
        Log.i(TAG, "--- App Usage Statistics (Last 24 Hours) ---");
        List<UsageStats> usageStatsList = new ArrayList<>(sortedMap.values());

        // Sort apps by usage time in foreground
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z2.getTotalTimeInForeground(), z1.getTotalTimeInForeground()));

        long totalTime = usageStatsList.stream().mapToLong(UsageStats::getTotalTimeInForeground).sum();

        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();
                long usageTimeMillis = usageStats.getTotalTimeInForeground();
                String usageDuration = getDurationBreakdown(usageTimeMillis);
                int usagePercentage = (int) (usageTimeMillis * 100 / totalTime);

                String appName = packageName.split("\\.")[packageName.split("\\.").length - 1];
                try {
                    ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
                    appName = getPackageManager().getApplicationLabel(ai).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    // Keep the package name if app label is not found
                }

                Log.i(TAG, "App: " + appName +
                        ", Package: " + packageName +
                        ", Usage Time: " + usageDuration +
                        ", Percentage: " + usagePercentage + "%");

            } catch (Exception e) {
                Log.e(TAG, "Error processing usage stats for an app", e);
            }
        }
        Log.i(TAG, "-------------------------------------------");
        Toast.makeText(this, "Usage data logged to Logcat", Toast.LENGTH_SHORT).show();
    }

    // Display the apps usage stats in the ListView
    public void showAppsUsage(Map<String, UsageStats> sortedMap) {
        ArrayList<App> apps = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(sortedMap.values());

        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z2.getTotalTimeInForeground(), z1.getTotalTimeInForeground()));

        long totalTime = usageStatsList.stream().mapToLong(UsageStats::getTotalTimeInForeground).sum();

        for (UsageStats usageStats : usageStatsList) {
            String packageName = usageStats.getPackageName();
            String appName = packageName; // Default to package name
            Drawable icon = getDrawable(R.drawable.no_image); // Default icon

            long usageTimeMillis = usageStats.getTotalTimeInForeground();

            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
                try {
                    CharSequence label = getPackageManager().getApplicationLabel(ai);
                    if (label != null) {
                        appName = label.toString();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting label for " + packageName, e);
                }

                try {
                    icon = getPackageManager().getApplicationIcon(ai);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting icon for " + packageName, e);
                }

                String usageDuration = getDurationBreakdown(usageTimeMillis);
                // Calculate percentage based on total time, avoid division by zero
                int usagePercentage = (totalTime > 0) ? (int) (usageTimeMillis * 100 / totalTime) : 0;

                // Include apps with any non-zero usage time
                if (usageTimeMillis > 0) {
                    App app = new App(icon, appName, usagePercentage, usageDuration);
                    apps.add(app);
                }

            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "App info not found for package: " + packageName, e);
                String usageDuration = getDurationBreakdown(usageTimeMillis);
                int usagePercentage = (totalTime > 0) ? (int) (usageTimeMillis * 100 / totalTime) : 0;
                if (usageTimeMillis > 0) {
                    App app = new App(icon, appName, usagePercentage, usageDuration);
                    apps.add(app);
                }
            }
        }

        AppsAdapter adapter = new AppsAdapter(this, apps);
        appsList.setAdapter(adapter);
        showHideItemsWhenShowApps();
    }

    // Helper methods for hiding/showing UI elements
    public void showHideNoPermission() {
        enableBtn.setVisibility(View.VISIBLE);
        permissionDescriptionTv.setVisibility(View.VISIBLE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.GONE);
        appsList.setVisibility(View.GONE);
        profileBtn.setVisibility(View.GONE);
        indexHome_btn.setVisibility(View.GONE);
        viewBarChartBtn.setVisibility(View.GONE);
    }

    public void showHideWithPermission() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.VISIBLE);
        usageTv.setVisibility(View.GONE);
        appsList.setVisibility(View.GONE);
        profileBtn.setVisibility(View.VISIBLE);
        indexHome_btn.setVisibility(View.GONE);
        viewBarChartBtn.setVisibility(View.VISIBLE);
    }

    public void showHideItemsWhenShowApps() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.VISIBLE);
        profileBtn.setVisibility(View.GONE);
        indexHome_btn.setVisibility(View.VISIBLE);
        viewBarChartBtn.setVisibility(View.GONE);
    }

    // Helper method to format the usage time
    private String getDurationBreakdown(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        return hours + " h " + minutes + " m " + seconds + " s";
    }

    // Check if the app info exists
    private boolean isAppInfoAvailable(UsageStats usageStats) {
        try {
            getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}