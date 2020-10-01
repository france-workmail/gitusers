package com.snarfapps.gitusers.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.snarfapps.gitusers.models.User;
import com.snarfapps.gitusers.models.UserDetail;


@Database(entities = {User.class, UserDetail.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
