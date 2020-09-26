package com.snarfapps.gitusers.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.snarfapps.gitusers.models.User;
import com.snarfapps.gitusers.models.UserDetail;

import java.util.List;

@Dao
public interface UserDao {

    @Query("Select * from user")
    List<User> getAllUsers();

    @Query("Select * from user WHERE login LIKE :searchKey")
    List<User> searchUserByName(String searchKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllUsers(List<User> users);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserDetail(UserDetail userDetail);

    @Query("Select * from userDetail WHERE id LIKE:userId")
    UserDetail getUserDetail(int userId);


    @Query("Select * from userdetail where id LIKE:userId AND notes NOT NULL")
    boolean userHasNotes(String userId);

    @Query("Select * from userdetail")
    List<UserDetail> getAllUserDetails();

}
