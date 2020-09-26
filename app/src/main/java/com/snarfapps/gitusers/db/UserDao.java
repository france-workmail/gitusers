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

    /**
     * Search using user and userdetail ententy
     */

//    @Query("Select * from user INNER JOIN userDetail ON notes LIKE:searchKey WHERE login LIKE:searchKey")
    //TODO set foreign keys for userdetail from user
    @Query("Select * from user INNER JOIN userDetail on user.id = userDetail.id WHERE user.login LIKE:searchKey OR userDetail.notes LIKE:searchKey")
    List<User> searchUserByNameOrNote(String searchKey);

}
