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
        setContentView(R.layout.activity_splash);

        // Delay the transition
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity after the delay
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 2000 ms = 2 seconds delay
    }
}
