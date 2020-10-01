package com.snarfapps.gitusers.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity()
public class User {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    public int id;

    @ColumnInfo(name = "login")
    @SerializedName("login")
    public String username;

    @ColumnInfo(name = "node_id")
    @SerializedName("node_id")
    public String nodeId;

    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    public String avatarUrl;

    @ColumnInfo(name = "gravatar_id")
    @SerializedName("gravatar_id")
    public String gravatarId;

    @ColumnInfo(name = "url")
    @SerializedName("url")
    public String url;

    @ColumnInfo(name = "html_url")
    @SerializedName("html_url")
    public String htmlUrl;

    @ColumnInfo(name = "followers_url")
    @SerializedName("followers_url")
    public String followersUrl;

    @ColumnInfo(name = "following_url")
    @SerializedName("following_url")
    public String followingUrl;

    @ColumnInfo(name = "gists_url")
    @SerializedName("gists_url")
    public String gistsUrl;

    @ColumnInfo(name = "starred_url")
    @SerializedName("starred_url")
    public String starredUrl;

    @ColumnInfo(name = "subscriptions_url")
    @SerializedName("subscriptions_url")
    public String subscriptionsUrl;

    @ColumnInfo(name = "organizations_url")
    @SerializedName("organizations_url")
    public String organizationsUrl;

    @ColumnInfo(name = "repos_url")
    @SerializedName("repos_url")
    public String reposUrl;

    @ColumnInfo(name = "events_url")
    @SerializedName("events_url")
    public String eventsUrl;

    @ColumnInfo(name = "received_events_url")
    @SerializedName("received_events_url")
    public String receivedEventsUrl;

    @ColumnInfo(name = "type")
    @SerializedName("type")
    public String type;

    @ColumnInfo(name = "site_admin")
    @SerializedName("site_admin")
    public boolean isSiteAdmin;


}
