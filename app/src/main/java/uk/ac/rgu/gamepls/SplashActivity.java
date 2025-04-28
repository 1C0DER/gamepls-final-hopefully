package uk.ac.rgu.gamepls;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Display splash screen for 2 seconds (you can adjust the time as needed)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After the delay, start the MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Close the SplashActivity
            }
        }, 2000);  // 2000 milliseconds = 2 seconds
    }
}
