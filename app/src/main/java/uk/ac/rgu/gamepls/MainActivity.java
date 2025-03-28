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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    Button enableBtn, profileBtn, showBtn, indexHome_btn;
    TextView permissionDescriptionTv, usageTv;
    ListView appsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBtn = findViewById(R.id.enable_btn);
        profileBtn = findViewById(R.id.profile_btn);
        showBtn = findViewById(R.id.show_btn);
        indexHome_btn = findViewById(R.id.indexHome_btn);
        permissionDescriptionTv = findViewById(R.id.permission_description_tv);
        usageTv = findViewById(R.id.usage_tv);
        appsList = findViewById(R.id.apps_list);

        loadStatistics();  // Load statistics on activity creation

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "No Name");
        String email = sharedPreferences.getString("email", "No Email");
        String username = sharedPreferences.getString("username", "No Username");

        // Pass the data to ProfileActivity
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getGrantStatus()) {
            showHideWithPermission();
            showBtn.setOnClickListener(view -> loadStatistics());
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

    // Load the statistics of apps usage for the last 24 hours
    public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 1000 * 3600 * 24, System.currentTimeMillis());

        appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());

        if (!appList.isEmpty()) {
            Map<String, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                sortedMap.put(usageStats.getPackageName(), usageStats);
            }
            showAppsUsage(sortedMap);
        }
    }

    // Display the apps usage stats
    public void showAppsUsage(Map<String, UsageStats> sortedMap) {
        ArrayList<App> apps = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(sortedMap.values());

        // Sort apps by usage time in foreground
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z2.getTotalTimeInForeground(), z1.getTotalTimeInForeground()));

        long totalTime = usageStatsList.stream().mapToLong(UsageStats::getTotalTimeInForeground).sum();

        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();
                Drawable icon = getDrawable(R.drawable.no_image);
                String appName = packageName.split("\\.")[packageName.split("\\.").length - 1];

                if (isAppInfoAvailable(usageStats)) {
                    ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
                    icon = getPackageManager().getApplicationIcon(ai);
                    appName = getPackageManager().getApplicationLabel(ai).toString();
                }

                String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
                int usagePercentage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);

                App app = new App(icon, appName, usagePercentage, usageDuration);
                apps.add(app);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Reverse the list to show most used apps first
        Collections.reverse(apps);
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
    }

    public void showHideWithPermission() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.VISIBLE);
        usageTv.setVisibility(View.GONE);
        appsList.setVisibility(View.GONE);
        profileBtn.setVisibility(View.VISIBLE);
        indexHome_btn.setVisibility(View.GONE);
    }

    public void showHideItemsWhenShowApps() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.VISIBLE);
        profileBtn.setVisibility(View.GONE);
        indexHome_btn.setVisibility(View.VISIBLE);
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
