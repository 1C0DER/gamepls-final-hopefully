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

public class ProfileActivity extends AppCompatActivity {

    TextView profileName, profileEmail, profileUsername, profilePassword;
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
