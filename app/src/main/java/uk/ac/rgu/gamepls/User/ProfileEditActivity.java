package uk.ac.rgu.gamepls.User;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import uk.ac.rgu.gamepls.R;

public class ProfileEditActivity extends AppCompatActivity {

    EditText editName, editEmail, editUsername, editPassword;
    Button saveButton;
    String nameUser, emailUser, usernameUser, passwordUser;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("users");

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);

        showData();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNameUpdated = isNameChanged();
                boolean isEmailUpdated = isEmailChanged();
                boolean isUsernameUpdated = isUsernameChanged();
                boolean isPasswordUpdated = isPasswordChanged();

                if (isNameUpdated || isEmailUpdated || isUsernameUpdated || isPasswordUpdated) {
                    Toast.makeText(ProfileEditActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                    // Log out the user after saving the changes
                    auth.signOut();

                    // Redirect the user to the login screen
                    Intent loginIntent = new Intent(ProfileEditActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the back stack
                    startActivity(loginIntent);
                    finish();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "No Changes Found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isNameChanged() {
        String newName = editName.getText().toString().trim();
        if (nameUser != null && !nameUser.trim().equals(newName)) {
            Log.d("ProfileEdit", "Updating name: " + newName);
            String userId = auth.getCurrentUser().getUid();
            reference.child(userId).child("name").setValue(newName);
            nameUser = newName;
            return true;
        } else {
            Log.d("ProfileEdit", "No name change detected.");
            return false;
        }
    }

    private boolean isEmailChanged() {
        String newEmail = editEmail.getText().toString().trim();
        if (emailUser != null && !emailUser.trim().equals(newEmail)) {
            Log.d("ProfileEdit", "Updating email: " + newEmail);
            String userId = auth.getCurrentUser().getUid();
            reference.child(userId).child("email").setValue(newEmail);
            emailUser = newEmail;
            return true;
        } else {
            Log.d("ProfileEdit", "No email change detected.");
            return false;
        }
    }

    private boolean isUsernameChanged() {
        String newUsername = editUsername.getText().toString().trim();
        if (usernameUser != null && !usernameUser.trim().equals(newUsername)) {
            Log.d("ProfileEdit", "Updating username: " + newUsername);
            String userId = auth.getCurrentUser().getUid();
            reference.child(userId).child("username").setValue(newUsername);

            // Update display name in Firebase Authentication
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("ProfileEdit", "User profile updated.");
                        } else {
                            Log.e("ProfileEdit", "Error updating user profile.", task.getException());
                            // Handle the error appropriately
                            Toast.makeText(ProfileEditActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show();
                        }
                    });

            usernameUser = newUsername;
            return true;
        } else {
            Log.d("ProfileEdit", "No username change detected.");
            return false;
        }
    }

    private boolean isPasswordChanged() {
        String newPassword = editPassword.getText().toString().trim();
        if (passwordUser != null && !passwordUser.trim().equals(newPassword)) {
            Log.d("ProfileEdit", "Updating password: " + newPassword);
            String userId = auth.getCurrentUser().getUid();
            reference.child(userId).child("password").setValue(newPassword);
            passwordUser = newPassword;
            return true;
        } else {
            Log.d("ProfileEdit", "No password change detected.");
            return false;
        }
    }

    public void showData() {
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");

        editName.setText(nameUser);
        editEmail.setText(emailUser);
        editUsername.setText(usernameUser);
        editPassword.setText(passwordUser);
    }
}
