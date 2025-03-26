package com.umarfarooq.i210497;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yourname.rollnumber.models.Story;

import java.util.ArrayList;
import java.util.List;

public class StoryViewActivity extends AppCompatActivity {

    private ImageView imageViewStory;
    private VideoView videoViewStory;
    private ProgressBar progressBar;
    private TextView textViewUsername;
    private ImageView imageViewProfile;
    private ImageView imageViewClose;
    
    private String userId;
    private String currentUserId;
    private List<Story> stories;
    private int currentStoryIndex = 0;
    
    private DatabaseReference mDatabase;
    
    // Handler for auto-progression of stories
    private Handler handler;
    private Runnable runnable;
    private static final long STORY_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize UI elements
        imageViewStory = findViewById(R.id.imageViewStory);
        videoViewStory = findViewById(R.id.videoViewStory);
        progressBar = findViewById(R.id.progressBar);
        textViewUsername = findViewById(R.id.textViewUsername);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        imageViewClose = findViewById(R.id.imageViewClose);
        
        // Initialize handler for story progression
        handler = new Handler();
        
        // Set touch listener for manual navigation
        View touchView = findViewById(R.id.touchView);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            private float x1, x2;
            private static final float MIN_DISTANCE = 150;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        handler.removeCallbacks(runnable);
                        return true;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            if (x2 > x1) {
                                // Right swipe (previous story)
                                showPreviousStory();
                            } else {
                                // Left swipe (next story)
                                showNextStory();
                            }
                        } else {
                            // Short tap, continue with current story
                            scheduleNextStory();
                        }
                        return true;
                }
                return false;
            }
        });
        
        // Set close button click listener
        imageViewClose.setOnClickListener(v -> finish());
        
        // Load stories
        loadStories();
    }

    private void loadStories() {
        stories = new ArrayList<>();
        
        mDatabase.child("stories").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stories.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    
                    // Only add non-expired stories
                    if (story != null && !story.isExpired()) {
                        stories.add(story);
                    }
                }
                
                if (stories.isEmpty()) {
                    // No stories available
                    finish();
                    return;
                }
                
                // Show first story
                showStory(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                finish();
            }
        });
    }
    
    private void showStory(int index) {
        if (index < 0 || index >= stories.size()) {
            finish();
            return;
        }
        
        currentStoryIndex = index;
        Story story = stories.get(index);
        
        // Mark story as viewed
        if (!story.isViewedBy(currentUserId)) {
            mDatabase.child("stories").child(userId).child(story.getId()).child("views").child(currentUserId).setValue(true);
        }
        
        // Set user info
        textViewUsername.setText(story.getUsername());
        Glide.with(this).load(story.getUserProfileUrl()).circleCrop().into(imageViewProfile);
        
        // Show media based on type
        if ("image".equals(story.getMediaType())) {
            imageViewStory.setVisibility(View.VISIBLE);
            videoViewStory.setVisibility(View.GONE);
            
            // Load image with Glide
            Glide.with(this).load(story.getMediaUrl()).into(imageViewStory);
        } else {
            imageViewStory.setVisibility(View.GONE);
            videoViewStory.setVisibility(View.VISIBLE);
            
            // Setup video
            videoViewStory.setVideoPath(story.getMediaUrl());
            videoViewStory.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoViewStory.start();
            });
        }
        
        // Schedule next story
        scheduleNextStory();
    }
    
    private void scheduleNextStory() {
        // Cancel any existing schedule
        handler.removeCallbacks(runnable);
        
        // Schedule next story
        runnable = () -> showNextStory();
        handler.postDelayed(runnable, STORY_DURATION);
    }
    
    private void showNextStory() {
        if (currentStoryIndex < stories.size() - 1) {
            showStory(currentStoryIndex + 1);
        } else {
            // End of stories, close the activity
            finish();
        }
    }
    
    private void showPreviousStory() {
        if (currentStoryIndex > 0) {
            showStory(currentStoryIndex - 1);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        
        if (videoViewStory.isPlaying()) {
            videoViewStory.pause();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (stories != null && !stories.isEmpty()) {
            scheduleNextStory();
            
            if (videoViewStory.getVisibility() == View.VISIBLE) {
                videoViewStory.start();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
