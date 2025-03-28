package com.yourname.rollnumber;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000; // 5 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Navigate to appropriate screen after splash delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndNavigate, SPLASH_DURATION);
    }
    
    private void checkUserAndNavigate() {
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        Intent intent;
        if (currentUser != null) {
            // User is signed in, check if profile is set up
            // This requires a database check
            checkIfProfileSetup(currentUser);
        } else {
            // No user is signed in, go to login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    private void checkIfProfileSetup(FirebaseUser user) {
        // Query Firebase to check if user profile exists
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            Intent intent;
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    // Profile is set up, go to home
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    // Profile not set up, go to profile setup
                    intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
                }
            } else {
                // Error occurred, default to profile setup
                intent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}
