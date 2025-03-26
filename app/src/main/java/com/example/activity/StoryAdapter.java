package com.umarfarooq.i210497.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yourname.rollnumber.R;
import com.yourname.rollnumber.StoryViewActivity;
import com.yourname.rollnumber.models.Story;
import com.yourname.rollnumber.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private Map<String, List<Story>> storiesMap;
    private String currentUserId;

    public StoryAdapter(Context context, List<User> users, String currentUserId) {
        this.context = context;
        this.users = users;
        this.storiesMap = new HashMap<>();
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        
        // Set username
        holder.textViewUsername.setText(user.getUsername());
        
        // Load profile image with Glide
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                .load(user.getProfileImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageViewProfile);
        } else {
            // Load default profile image
            holder.imageViewProfile.setImageResource(R.drawable.default_profile);
        }
        
        // Check if all stories of this user are viewed
        List<Story> userStories = storiesMap.get(user.getId());
        boolean allStoriesViewed = true;
        
        if (userStories != null && !userStories.isEmpty()) {
            for (Story story : userStories) {
                if (!story.isViewedBy(currentUserId)) {
                    allStoriesViewed = false;
                    break;
                }
            }
        } else {
            // No stories available
            allStoriesViewed = true;
        }
        
        // Set story ring color based on view status
        if (allStoriesViewed) {
            holder.imageViewStoryRing.setImageResource(R.drawable.story_ring_viewed);
        } else {
            holder.imageViewStoryRing.setImageResource(R.drawable.story_ring_new);
        }
        
        // Set click listener to view stories
        holder.itemView.setOnClickListener(v -> {
            if (userStories != null && !userStories.isEmpty()) {
                Intent intent = new Intent(context, StoryViewActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void setStories(Map<String, List<Story>> storiesMap) {
        this.storiesMap = storiesMap;
        notifyDataSetChanged();
    }
    
    public void addStories(String userId, List<Story> stories) {
        this.storiesMap.put(userId, stories);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewProfile;
        public ImageView imageViewStoryRing;
        public TextView textViewUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            imageViewStoryRing = itemView.findViewById(R.id.imageViewStoryRing);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
        }
    }
}
