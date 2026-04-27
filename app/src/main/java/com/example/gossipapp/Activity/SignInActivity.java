package com.example.gossipapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gossipapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ImageView googleSignInBtn;
    private FirebaseAuth mAuth;
    EditText emailAddressEdt,usernameEdt,passwordEdt;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 100;
    TextView loginBtn;
    Button signinBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        googleSignInBtn = findViewById(R.id.googleSignIn_btn);

        mAuth = FirebaseAuth.getInstance();

        configureGoogleSignIn();

        googleSignInBtn.setOnClickListener(v -> signInWithGoogle());
        emailAddressEdt=findViewById(R.id.email_address);
        usernameEdt=findViewById(R.id.user_name);
        passwordEdt=findViewById(R.id.email_password);
        signinBtn=findViewById(R.id.signinbutton);
        loginBtn=findViewById(R.id.login_txt);
        signinBtn=findViewById(R.id.signinbutton);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertUserData();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                startActivity(intent);


            }
        });
    }

    private void insertUserData() {
        String email = emailAddressEdt.getText().toString().trim();
        String username = usernameEdt.getText().toString().trim();
        String password = passwordEdt.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailAddressEdt.setError("Enter a valid email");
            return;
        }
        if (username.isEmpty() || username.length() < 3) {
            usernameEdt.setError("Username must be at least 3 characters");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEdt.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the authenticated user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            HashMap<String, Object> data = new HashMap<>();
                            data.put("email", email);
                            data.put("username", username);
                            data.put("password", password);
                            data.put("profileImage", "");
                            data.put("createdAt", Timestamp.now());
                            data.put("isActive", true);
                            data.put("notesAccessExpiry", 0);
                            data.put("role", "user"); // 👈 default user role
                            data.put("isHidden", false);

                            db.collection("Users").document(uid)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignInActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignInActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });


                                                            }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclintId))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In Failed: " + e.getStatusCode());
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(SignInActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                }
            } else {
                Log.e(TAG, "Firebase Authentication Failed", task.getException());
                Toast.makeText(SignInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
