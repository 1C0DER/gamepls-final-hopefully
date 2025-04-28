package uk.ac.rgu.gamepls;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.rgu.gamepls.R;
import uk.ac.rgu.gamepls.User.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Make sure you have a splash layout

        // Delay the transition to the LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity after the delay
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // Finish SplashActivity so it doesn't stay in the back stack
                finish();
            }
        }, 2000); // 2000 ms = 2 seconds delay, adjust this time as needed
    }
}
