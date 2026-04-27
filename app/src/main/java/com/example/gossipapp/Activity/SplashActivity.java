package com.example.gossipapp.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.gossipapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView startMsg = findViewById(R.id.startmsg_btn);

        // Show static "Developed By" first
        startMsg.setText("Developed By \n");
        TypeWriter typeWriter = new TypeWriter(startMsg, this);
        typeWriter.animateText("Umang Markana", 120);

        // Wait until typewriter finishes before moving to next screen
        long totalDelay = 150 * "Umang Markana".length() + 500;
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, totalDelay);
    }
}
