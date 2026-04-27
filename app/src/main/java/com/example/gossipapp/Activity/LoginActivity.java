package com.example.gossipapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gossipapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText usernameField, passwordField;
    private Button loginBtn;
    private TextView signTxt;
    private ImageView eyeIcon;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USERNAME = "username";

    private LottieAnimationView loadingAnim;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginbutton);
        signTxt = findViewById(R.id.signin_txt);
        loadingAnim = findViewById(R.id.loginLoadingAnim);

        passwordField.setOnLongClickListener(v -> {
            // Show password while pressed
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordField.setSelection(passwordField.getText().length());
            return true; // Consume the long press
        });

        passwordField.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Hide password when user releases
                    passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordField.setSelection(passwordField.getText().length());
                    break;
            }
            return false;
        });


        // 🔹 FCM Token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d("TokenDetails", "Token Failed to receive");
                return;
            }
            String token = task.getResult();
            Log.d("TOKEN", token);
        });

        signTxt.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignInActivity.class))
        );

        loginBtn.setOnClickListener(v -> loginUser());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users")
                    .document(user.getUid())
                    .update("isOnline", true);
        }

    }


    private void loginUser() {
        String rawUsername = usernameField.getText().toString().trim();
        String username = rawUsername.toLowerCase();
        String password = passwordField.getText().toString();

        // Validate inputs
        if (rawUsername.isEmpty()) {
            usernameField.setError("Please enter your username");
            return;
        }
        if (password.isEmpty()) {
            passwordField.setError("Please enter your password");
            return;
        }
        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            return;
        }

        // ✅ Show Lottie animation when login starts
        loadingAnim.setVisibility(View.VISIBLE);
        loadingAnim.playAnimation();

        db.collection("Users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    // Hide animation for early failure cases
                    if (!task.isSuccessful() || task.getResult().isEmpty()) {
                        loadingAnim.cancelAnimation();
                        loadingAnim.setVisibility(View.GONE);
                    }

                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Firestore lookup error", task.getException());
                        Toast.makeText(this,
                                "Error checking username: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = task.getResult()
                            .getDocuments()
                            .get(0)
                            .getString("email");

                    if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this,
                                "Invalid email on record. Please contact support.",
                                Toast.LENGTH_LONG).show();
                        loadingAnim.cancelAnimation();
                        loadingAnim.setVisibility(View.GONE);
                        return;
                    }

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, authTask -> {
                                // ✅ Hide animation after login attempt
                                loadingAnim.cancelAnimation();
                                loadingAnim.setVisibility(View.GONE);

                                if (authTask.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");

                                    // Store token
                                    FirebaseMessaging.getInstance().getToken()
                                            .addOnSuccessListener(token -> {
                                                String userDoc = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                FirebaseFirestore.getInstance()
                                                        .collection("Users")
                                                        .document(userDoc)
                                                        .update("fcmToken", token)
                                                        .addOnSuccessListener(a -> Log.d("FCM", "Token saved"))
                                                        .addOnFailureListener(e -> Log.e("FCM", "Token save fail", e));
                                            });

                                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                    prefs.edit()
                                            .putBoolean(KEY_LOGGED_IN, true)
                                            .putString(KEY_USERNAME, username)
                                            .apply();

                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                }
 else {
                                    Exception e = authTask.getException();
                                    Log.w(TAG, "signInWithEmail:failure", e);

                                    if (e instanceof FirebaseAuthInvalidUserException) {
                                        Toast.makeText(this,
                                                "No user found with this email.",
                                                Toast.LENGTH_SHORT).show();
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        Toast.makeText(this,
                                                "Incorrect password.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this,
                                                "Authentication failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                });
    }


}
