package com.applikey.mattermost.models.post;

import com.applikey.mattermost.models.user.User;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Post extends RealmObject {

    public static final String FIELD_NAME_CHANNEL_ID = "channelId";
    public static final String FIELD_NAME_CHANNEL_CREATE_AT = "createdAt";

    @PrimaryKey
    @SerializedName("id")
    private String id;

    @SerializedName("channel_id")
    private String channelId;

    @SerializedName("create_at")
    private long createdAt;

    @SerializedName("user_id")
    private String userId;
    private User user;

    @SerializedName("message")
    private String message;

    // Application-specific fields
    private int priority;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public static final Comparator<Post> COMPARATOR_BY_PRIORITY = (o1, o2)
            -> o2.getPriority() - o1.getPriority();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Post post = (Post) o;

        if (getCreatedAt() != post.getCreatedAt())
            return false;
        if (getPriority() != post.getPriority())
            return false;
        if (!getId().equals(post.getId()))
            return false;
        if (!getChannelId().equals(post.getChannelId()))
            return false;
        if (!getUserId().equals(post.getUserId()))
            return false;
        return getMessage().equals(post.getMessage());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getChannelId().hashCode();
        result = 31 * result + (int) (getCreatedAt() ^ (getCreatedAt() >>> 32));
        result = 31 * result + getUserId().hashCode();
        result = 31 * result + getMessage().hashCode();
        result = 31 * result + getPriority();
        return result;
    }
}
