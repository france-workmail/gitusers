package com.snarfapps.gitusers;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snarfapps.gitusers.models.User;
import com.snarfapps.gitusers.models.UserDetail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    public static String USERNAME_EXTRA_PARAMS = "username";
    public static String USERID_EXTRA_PARAMS = "userid";


    /**
     * Layouts
     */
    EditText etNotes;
    TextView tvUsername, tvFollowers,tvFollowing, tvName, tvCompany, tvBlog;
    ImageView ivAvatar;
    Button btnSave;
    ImageButton ibBack;

    String userName;
    int userId;
    UserDetail userDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvFollowers = findViewById(R.id.tvFollowers);
        tvFollowing = findViewById(R.id.tvFollowing);
        tvName = findViewById(R.id.tvName);
        tvCompany = findViewById(R.id.tvCompany);
        tvBlog = findViewById(R.id.tvBlog);
        etNotes = findViewById(R.id.etNotes);


        ivAvatar = findViewById(R.id.ivAvatar);
        btnSave = findViewById(R.id.btnSave);
        ibBack = findViewById(R.id.ibBack);


        userName = getIntent().getStringExtra(USERNAME_EXTRA_PARAMS);
        userId  = getIntent().getIntExtra(USERID_EXTRA_PARAMS, -1);

        // Initially load the data from db
        loadFromDb();

        btnSave.setOnClickListener(v -> {

            // Add the notes to user info.
            // then save the changes to db
            userDetail.notes = etNotes.getText().toString();
            saveUserDetail();
        });
        ibBack.setOnClickListener(v -> finish());
    }



    void loadUserProfile(String username){
        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_USER_PROFILE_URL + username,
                response -> {

                    Log.e("Fetch", "User detail fetched from server");

                    Type typeToken = new TypeToken<UserDetail>(){}.getType();


                    String notesHolder = "";
                    //get the notes first so it wont be overridden by new data
                    if(userDetail!=null && userDetail.notes!=null)
                        notesHolder = userDetail.notes;

                    // set the updated user detail data
                    userDetail = new Gson().fromJson(response,typeToken);

                    //return the notes to overridden user data
                    userDetail.notes = notesHolder;

                    //update the user detail
                    saveUserDetail();


                    bindUserDetail(userDetail);

                },
                error -> {

                });

        NetworkQueue.getInstance().addQueue(request);
    }


    void bindUserDetail(UserDetail user){
        tvUsername.setText(getIntent().getStringExtra(USERNAME_EXTRA_PARAMS));
        tvFollowers.setText("Followers: " + user.followers);
        tvFollowing.setText("Following: "+ user.following);
        tvName.setText("Name: " + user.name);
        tvCompany.setText("Company: "+ (user.company==null?"":user.company));
        tvBlog.setText("Blog: "+ (user.blog == null? "":user.blog));
        etNotes.setText(user.notes);

        Glide.with(this).load(user.avatarUrl).into(ivAvatar);
    }

    /**
     * Database operations
     */


    /**
     * Checks if the currently selected user has a
     * db version of its data.
     */
    @SuppressLint("StaticFieldLeak")
    void loadFromDb(){

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                userDetail = Constants.db.userDao().getUserDetail(userId);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //if no user is in db, load data from server

                if(userDetail != null) {
                    //user has data in db so directly bind it
                    bindUserDetail(userDetail);
                }

                //regardless if local data is present or not
                // we need to fetch from server to get an updated
                // copy of user data
                loadUserProfile(userName);

            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    void saveUserDetail(){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {


                Constants.db.userDao().insertUserDetail(userDetail);


                List<UserDetail> details=  Constants.db.userDao().getAllUserDetails();
                Log.e("DB Operation", "Inserted/Added new user. Current user details size: "+ details.size());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //notify main activity list
                MainActivity.usersAdapter.notifyDataSetChanged();

            }
        }.execute();
    }
}