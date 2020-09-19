package com.snarfapps.gitusers.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "login")
    public String username;

    @ColumnInfo(name = "node_id")
    public String nodeId;

    @ColumnInfo(name = "avatar_url")
    public String avatarUrl;

    @ColumnInfo(name = "gravatar_id")
    public String gravatarId;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "html_url")
    public String htmlUrl;

    @ColumnInfo(name = "followers_url")
    public String followersUrl;

    @ColumnInfo(name = "following_url")
    public String followingUrl;

    @ColumnInfo(name = "gists_url")
    public String gistsUrl;

    @ColumnInfo(name = "starred_url")
    public String starredUrl;

    @ColumnInfo(name = "subscriptions_url")
    public String subscriptionsUrl;

    @ColumnInfo(name = "organizations_url")
    public String organizationsUrl;

    @ColumnInfo(name = "repos_url")
    public String reposUrl;

    @ColumnInfo(name = "events_url")
    public String eventsUrl;

    @ColumnInfo(name = "received_events_url")
    public String receivedEventsUrl;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "site_admin")
    public boolean isSiteAdmin;

}
