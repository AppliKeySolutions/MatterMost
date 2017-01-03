package com.applikey.mattermost.models.post;

import android.support.annotation.Nullable;

import com.applikey.mattermost.models.RealmString;
import com.applikey.mattermost.models.user.User;
import com.google.gson.annotations.SerializedName;
import com.vdurmont.emoji.EmojiParser;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Post extends RealmObject {

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_CHANNEL_ID = "channelId";
    public static final String FIELD_NAME_CHANNEL_CREATE_AT = "createdAt";

    @PrimaryKey
    @SerializedName("id")
    private String id;
    @SerializedName("channel_id")
    private String channelId;
    @Nullable
    @SerializedName("root_id")
    private String rootId;
    @Nullable
    private Post rootPost;
    @Nullable
    @SerializedName("parent_id")
    private String parentId;
    @SerializedName("create_at")
    private long createdAt;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("message")
    private String message;
    @SerializedName("filenames")
    private RealmList<RealmString> filenames;
    // Application-specific fields
    private int priority;
    private User author;
    private boolean mSent = true;

    public Post() {

    }

    private Post(Builder builder) {
        setId(builder.id);
        setChannelId(builder.channelId);
        setCreatedAt(builder.createdAt);
        setUserId(builder.userId);
        setMessage(builder.message);
        setSent(builder.sent);
    }

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

    public String getMessage() {
        return EmojiParser.parseToUnicode(message);
    }

    public void setMessage(String message) {
        this.message = EmojiParser.parseToAliases(message);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Nullable
    public String getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable String parentId) {
        this.parentId = parentId;
    }

    @Nullable
    public String getRootId() {
        return rootId;
    }

    public void setRootId(@Nullable String rootId) {
        this.rootId = rootId;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean isSent() {
        return mSent;
    }

    public void setSent(boolean sent) {
        mSent = sent;
    }

    @Nullable
    public Post getRootPost() {
        return rootPost;
    }

    public void setRootPost(@Nullable Post rootPost) {
        this.rootPost = rootPost;
    }

    public List<RealmString> getFilenames() {
        return filenames;
    }

    public void setFilenames(RealmList<RealmString> filenames) {
        this.filenames = filenames;
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getChannelId().hashCode();
        result = 31 * result + (int) (getCreatedAt() ^ (getCreatedAt() >>> 32));
        result = 31 * result + getUserId().hashCode();
        result = 31 * result + getMessage().hashCode();
        result = 31 * result + getPriority();
        result = 31 * result + (getAuthor() != null ? getAuthor().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Post post = (Post) o;

        if (getCreatedAt() != post.getCreatedAt()) {
            return false;
        }
        if (getParentId() != null && !getParentId().equals(post.getParentId())) {
            return false;
        }
        if (getRootId() != null && !getRootId().equals(post.getRootId())) {
            return false;
        }
        if (getPriority() != post.getPriority()) {
            return false;
        }
        if (!getId().equals(post.getId())) {
            return false;
        }
        if (!getChannelId().equals(post.getChannelId())) {
            return false;
        }
        if (!getUserId().equals(post.getUserId())) {
            return false;
        }
        if (!getMessage().equals(post.getMessage())) {
            return false;
        }
        return getAuthor() != null
                ? getAuthor().equals(post.getAuthor())
                : post.getAuthor() == null;
    }

    public static int COMPARATOR_BY_CREATE_AT(Post post1, Post post2) {
        return (int) (post1.getCreatedAt() - post2.getCreatedAt());
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", channelId='" + channelId + '\'' +
                ", rootId='" + rootId + '\'' +
                ", rootPost=" + rootPost +
                ", parentId='" + parentId + '\'' +
                ", createdAt=" + createdAt +
                ", userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                ", priority=" + priority +
                ", author=" + author +
                '}';
    }

    public static final class Builder {

        private String id;
        private String channelId;
        private long createdAt;
        private String userId;
        private String message;
        private boolean sent;

        public Builder() {
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder channelId(String val) {
            channelId = val;
            return this;
        }

        public Builder createdAt(long val) {
            createdAt = val;
            return this;
        }

        public Builder userId(String val) {
            userId = val;
            return this;
        }

        public Builder message(String val) {
            message = val;
            return this;
        }

        public Builder sent(boolean val) {
            sent = val;
            return this;
        }

        public Post build() {
            return new Post(this);
        }
    }
}
