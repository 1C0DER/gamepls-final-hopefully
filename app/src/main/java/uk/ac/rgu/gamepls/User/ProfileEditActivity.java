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

import uk.ac.rgu.gamepls.R;

public class ProfileEditActivity extends AppCompatActivity {

    EditText editName, editEmail, editUsername, editPassword;
    Button saveButton;
    String nameUser, emailUser, usernameUser, passwordUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
                boolean isPasswordUpdated = isPasswordChanged();

                if (isNameUpdated || isEmailUpdated || isPasswordUpdated) {
                    Toast.makeText(ProfileEditActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                    finish();  // Close the ProfileEditActivity after saving
                } else {
                    Toast.makeText(ProfileEditActivity.this, "No Changes Found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isNameChanged() {
        String newName = editName.getText().toString().trim();
        if (nameUser != null && !nameUser.trim().equals(newName)) {
            Log.d("ProfileEdit", "Updating name: " + newName);  // Log the change
            reference.child(usernameUser).child("name").setValue(newName);  // Use UID instead of email
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
            Log.d("ProfileEdit", "Updating email: " + newEmail);  // Log the change
            reference.child(usernameUser).child("email").setValue(newEmail);  // Use UID instead of email
            emailUser = newEmail;
            return true;
        } else {
            Log.d("ProfileEdit", "No email change detected.");
            return false;
        }
    }

    private boolean isPasswordChanged() {
        String newPassword = editPassword.getText().toString().trim();
        if (passwordUser != null && !passwordUser.trim().equals(newPassword)) {
            Log.d("ProfileEdit", "Updating password: " + newPassword);  // Log the change
            reference.child(usernameUser).child("password").setValue(newPassword);  // Use UID instead of email
            passwordUser = newPassword;
            return true;
        } else {
            Log.d("ProfileEdit", "No password change detected.");
            return false;
        }
    }


    public void showData() {
        // Assuming data is passed through Intent or SharedPreferences
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
