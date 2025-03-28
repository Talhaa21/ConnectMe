package com.umarfarooq.i210497.models;

import java.util.HashMap;
import java.util.Map;

public class Story {
    private String id;
    private String userId;
    private String userProfileUrl;
    private String username;
    private String mediaUrl;
    private String mediaType; // "image" or "video"
    private long timestamp;
    private long expiryTime; // 24 hours after posting
    private Map<String, Boolean> views; // User IDs who viewed the story

    // Required for Firebase
    public Story() {
    }

    public Story(String id, String userId, String userProfileUrl, String username, String mediaUrl, 
                 String mediaType, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userProfileUrl = userProfileUrl;
        this.username = username;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.timestamp = timestamp;
        this.expiryTime = timestamp + (24 * 60 * 60 * 1000); // 24 hours in milliseconds
        this.views = new HashMap<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Map<String, Boolean> getViews() {
        return views;
    }

    public void setViews(Map<String, Boolean> views) {
        this.views = views;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public void addView(String userId) {
        if (views == null) {
            views = new HashMap<>();
        }
        views.put(userId, true);
    }

    public boolean isViewedBy(String userId) {
        return views != null && views.containsKey(userId);
    }

    public int getViewCount() {
        return views != null ? views.size() : 0;
    }
}
