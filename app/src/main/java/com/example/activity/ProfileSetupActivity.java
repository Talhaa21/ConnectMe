package com.umarfarooq.i210497;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextBio;
    private ImageView imageViewProfilePic, imageViewCoverPhoto;
    private Button buttonSave;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference storageReference;
    
    private Uri profileImageUri = null;
    private Uri coverImageUri = null;
    
    // Activity result launchers for image selection
    private final ActivityResultLauncher<String> pickProfileImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    imageViewProfilePic.setImageURI(profileImageUri);
                }
            });
            
    private final ActivityResultLauncher<String> pickCoverImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    coverImageUri = uri;
                    imageViewCoverPhoto.setImageURI(coverImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextBio = findViewById(R.id.editTextBio);
        imageViewProfilePic = findViewById(R.id.imageViewProfilePic);
        imageViewCoverPhoto = findViewById(R.id.imageViewCoverPhoto);
        buttonSave = findViewById(R.id.buttonSave);

        // Set click listeners
        imageViewProfilePic.setOnClickListener(v -> pickProfileImage.launch("image/*"));
        imageViewCoverPhoto.setOnClickListener(v -> pickCoverImage.launch("image/*"));
        buttonSave.setOnClickListener(v -> saveUserProfile());
    }

    private void saveUserProfile() {
        String username = editTextUsername.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();

        // Validate username
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // User not logged in, redirect to login
            Toast.makeText(this, "You need to be logged in to set up profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileSetupActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Show loading indicator
        // progressBar.setVisibility(View.VISIBLE);
        
        // Upload profile image if selected
        if (profileImageUri != null) {
            uploadProfileImage(user.getUid(), username, bio);
        } else if (coverImageUri != null) {
            // If only cover photo is selected
            uploadCoverImage(user.getUid(), username, bio, null);
        } else {
            // No images selected, just save profile info
            saveUserData(user.getUid(), username, bio, null, null);
        }
    }
    
    private void uploadProfileImage(String userId, String username, String bio) {
        StorageReference profileRef = storageReference.child("profile_images/" + userId + ".jpg");
        
        profileRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileUrl = uri.toString();
                        
                        // Upload cover image if selected
                        if (coverImageUri != null) {
                            uploadCoverImage(userId, username, bio, profileUrl);
                        } else {
                            saveUserData(userId, username, bio, profileUrl, null);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    // progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileSetupActivity.this, "Failed to upload profile image: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }
    
    private void uploadCoverImage(String userId, String username, String bio, String profileUrl) {
        StorageReference coverRef = storageReference.child("cover_images/" + userId + ".jpg");
        
        coverRef.putFile(coverImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    coverRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String coverUrl = uri.toString();
                        saveUserData(userId, username, bio, profileUrl, coverUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    // progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileSetupActivity.this, "Failed to upload cover image: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                    
                    // Save user data with only profile image if available
                    if (profileUrl != null) {
                        saveUserData(userId, username, bio, profileUrl, null);
                    }
                });
    }
    
    private void saveUserData(String userId, String username, String bio, String profileUrl, String coverUrl) {
        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("bio", bio);
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("createdAt", System.currentTimeMillis());
        
        if (profileUrl != null) {
            userData.put("profileImageUrl", profileUrl);
        }
        
        if (coverUrl != null) {
            userData.put("coverImageUrl", coverUrl);
        }
        
        // Save user data to database
        mDatabase.child("users").child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    // Hide loading indicator
                    // progressBar.setVisibility(View.GONE);
                    
                    Toast.makeText(ProfileSetupActivity.this, "Profile setup completed", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to home screen
                    Intent intent = new Intent(ProfileSetupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    // progressBar.setVisibility(View.GONE);
                    
                    Toast.makeText(ProfileSetupActivity.this, "Failed to save profile: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }
}
