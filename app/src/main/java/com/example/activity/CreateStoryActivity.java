package com.yourname.rollnumber;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yourname.rollnumber.models.Story;

import java.util.UUID;

public class CreateStoryActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private VideoView videoPreview;
    private Button buttonSelectImage, buttonSelectVideo, buttonPost;
    
    private Uri mediaUri;
    private String mediaType;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference storageReference;
    
    // Activity result launchers for media selection
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mediaUri = result.getData().getData();
                    mediaType = "image";
                    
                    // Show image preview
                    imagePreview.setVisibility(View.VISIBLE);
                    videoPreview.setVisibility(View.GONE);
                    imagePreview.setImageURI(mediaUri);
                }
            });
            
    private final ActivityResultLauncher<Intent> pickVideo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    mediaUri = result.getData().getData();
                    mediaType = "video";
                    
                    // Show video preview
                    imagePreview.setVisibility(View.GONE);
                    videoPreview.setVisibility(View.VISIBLE);
                    videoPreview.setVideoURI(mediaUri);
                    videoPreview.start();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        imagePreview = findViewById(R.id.imagePreview);
        videoPreview = findViewById(R.id.videoPreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectVideo = findViewById(R.id.buttonSelectVideo);
        buttonPost = findViewById(R.id.buttonPost);

        // Set click listeners
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });
        
        buttonSelectVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            pickVideo.launch(intent);
        });
        
        buttonPost.setOnClickListener(v -> {
            if (mediaUri != null) {
                uploadStory();
            } else {
                Toast.makeText(this, "Please select an image or video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadStory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in to post a story", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateStoryActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Show loading indicator
        // progressBar.setVisibility(View.VISIBLE);
        
        // Generate a unique ID for the story
        String storyId = UUID.randomUUID().toString();
        
        // Reference to where the media will be stored
        StorageReference mediaRef = storageReference.child("stories/" + user.getUid() + "/" + storyId);
        
        // Upload the media file
        mediaRef.putFile(mediaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    mediaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String mediaUrl = uri.toString();
                        
                        // Get user data and create story
                        getUserDataAndCreateStory(user.getUid(), storyId, mediaUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    // progressBar.setVisibility(View.GONE);
                    
                    Toast.makeText(CreateStoryActivity.this, "Failed to upload story: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                });
    }
    
    private void getUserDataAndCreateStory(String userId, String storyId, String mediaUrl) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                
                // Create story object
                long timestamp = System.currentTimeMillis();
                Story story = new Story(storyId, userId, profileImageUrl, username, mediaUrl, mediaType, timestamp);
                
                // Save story to database
                mDatabase.child("stories").child(userId).child(storyId).setValue(story)
                        .addOnSuccessListener(aVoid -> {
// Hide loading indicator
                            // progressBar.setVisibility(View.GONE);
                            
                            Toast.makeText(CreateStoryActivity.this, "Story posted successfully", Toast.LENGTH_SHORT).show();
                            
                            // Navigate back to home screen
                            Intent intent = new Intent(CreateStoryActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Hide loading indicator
                            // progressBar.setVisibility(View.GONE);
                            
                            Toast.makeText(CreateStoryActivity.this, "Failed to save story: " + e.getMessage(), 
                                          Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Hide loading indicator
                // progressBar.setVisibility(View.GONE);
                
                Toast.makeText(CreateStoryActivity.this, "Failed to load user data: " + databaseError.getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        });
    }
}
