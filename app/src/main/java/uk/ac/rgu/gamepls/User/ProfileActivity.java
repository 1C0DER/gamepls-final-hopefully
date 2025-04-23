package uk.ac.rgu.gamepls.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.ac.rgu.gamepls.MainActivity;
import uk.ac.rgu.gamepls.R;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileActivity extends AppCompatActivity {

    TextView profileName, profileEmail, profileUsername, profilePassword, totalHoursNum, dayHoursNum;
    Button editProfile;
    ImageButton backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize the views
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        profilePassword = findViewById(R.id.profilePassword);
        totalHoursNum = findViewById(R.id.totalHoursNum);  // TextView to display total hours for all time
        dayHoursNum = findViewById(R.id.dayHoursNum);  // TextView to display today's hours
        backArrow = findViewById(R.id.backArrow);
        editProfile = findViewById(R.id.editButton);

        // Show user data when the activity is created
        showAllUserData();

        // Set the click listener for the edit profile button
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passUserData();
            }
        });

        // Set the click listener for the back arrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Calculate and display the total usage time for all time
        calculateTotalUsageTime();

        // Calculate and display the total usage time for today
        calculateTodayUsageTime();
    }

    // Fetch user data from Firebase and display it
    public void showAllUserData() {
        // Get the current user's ID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the "users" node in Firebase Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Retrieve the user's data from the database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract user data from the snapshot
                    String nameFromDB = dataSnapshot.child("name").getValue(String.class);
                    String emailFromDB = dataSnapshot.child("email").getValue(String.class);
                    String usernameFromDB = dataSnapshot.child("username").getValue(String.class);
                    String passwordFromDB = dataSnapshot.child("password").getValue(String.class);

                    // Display the data in the TextViews
                    profileName.setText(nameFromDB);
                    profileEmail.setText(emailFromDB);
                    profileUsername.setText(usernameFromDB);
                    profilePassword.setText(passwordFromDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Log.e("FirebaseError", "Error retrieving user data: " + error.getMessage());
            }
        });
    }

    // Calculate the total time spent on apps and update the TextView
    private void calculateTotalUsageTime() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long totalTime = 0;

        // Get the current date
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 3600 * 24 * 7;  // Past 7 days

        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        // Sum up the usage time for each app
        for (UsageStats usageStats : appList) {
            totalTime += usageStats.getTotalTimeInForeground();
        }

        // Convert total time in milliseconds to hours
        float totalHours = totalTime / 3600000f;

        // Update the total hours TextView
        totalHoursNum.setText(String.format("%.2f", totalHours));  // Set to 2 decimal places
    }

    // Calculate the total time spent on apps today and update the TextView
    private void calculateTodayUsageTime() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long totalTimeToday = 0;

        // Get the current date
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 3600 * 24;  // Current day

        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        // Sum up the usage time for each app
        for (UsageStats usageStats : appList) {
            totalTimeToday += usageStats.getTotalTimeInForeground();
        }

        // Convert total time in milliseconds to hours
        float totalHoursToday = totalTimeToday / 3600000f;

        // Update the today's hours TextView
        dayHoursNum.setText(String.format("%.2f", totalHoursToday));  // Set to 2 decimal places
    }

    // Pass the user data to the ProfileEditActivity
    public void passUserData() {
        // Get the user data from the TextViews
        String userName = profileName.getText().toString();
        String userEmail = profileEmail.getText().toString();
        String userUsername = profileUsername.getText().toString();
        String userPassword = profilePassword.getText().toString();

        // Create an Intent to navigate to the ProfileEditActivity
        Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);

        // Put the user data in the Intent
        intent.putExtra("name", userName);
        intent.putExtra("email", userEmail);
        intent.putExtra("username", userUsername);
        intent.putExtra("password", userPassword);

        // Start the ProfileEditActivity
        startActivity(intent);
    }
}
