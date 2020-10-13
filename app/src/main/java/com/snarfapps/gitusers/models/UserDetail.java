package com.snarfapps.gitusers.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity ()
public class UserDetail {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    public int id;

    @ColumnInfo(name = "login")
    @SerializedName("login")
    public String login;


    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    public String avatarUrl;
    /**
     * User details
     */

    @ColumnInfo(name = "name")
    @SerializedName("name")
    public String name;


    @ColumnInfo(name = "company")
    @SerializedName("company")
    public String company;


    @ColumnInfo(name = "blog")
    @SerializedName("blog")
    public String blog;

    @ColumnInfo(name = "location")
    @SerializedName("location")
    public String location;

    @ColumnInfo(name = "email")
    @SerializedName("email")
    public String email;

    @ColumnInfo(name = "hireable")
    @SerializedName("hireable")
    public String hireable;

    @ColumnInfo(name = "bio")
    @SerializedName("bio")
    public String bio;

    @ColumnInfo(name = "twitter_username")
    @SerializedName("twitter_username")
    public String twitterUsername;

    @ColumnInfo(name = "public_repos")
    @SerializedName("public_repos")
    public int publicRepos;


    @ColumnInfo(name = "public_gists")
    @SerializedName("public_gists")
    public int publicGists;

    @ColumnInfo(name = "followers")
    @SerializedName("followers")
    public int followers;

    @ColumnInfo(name = "following")
    @SerializedName("following")
    public int following;


    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    public String createdAt;

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    public String updatedAt;

    @ColumnInfo(name = "notes")
    public String notes;
}
