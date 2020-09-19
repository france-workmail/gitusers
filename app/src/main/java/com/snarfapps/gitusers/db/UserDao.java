package com.snarfapps.gitusers.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.snarfapps.gitusers.models.User;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface UserDao {

    @Query("Select * from user")
    List<User> getAllUsers();

    @Query("Select * from user WHERE login LIKE :searchKey")
    List<User> searchUserByName(String searchKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllUsers(User...users);

    @Insert
    void addUser(User user);

}
