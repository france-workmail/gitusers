package com.snarfapps.gitusers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

public class ProfileActivity extends AppCompatActivity {

    public static String USERNAME_EXTRA_PARAMS = "username";


    /**
     * Layouts
     */
    TextView tvUsername, tvFollowers,tvFollowing, tvName, tvCompany, tvBlog, tvNotes;
    ImageView ivAvatar;
    Button btnSave;
    ImageButton ibBack;


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
        tvNotes = findViewById(R.id.tvNotes);


        ivAvatar = findViewById(R.id.ivAvatar);
        btnSave = findViewById(R.id.btnSave);
        ibBack = findViewById(R.id.ibBack);

        loadUserProfile(getIntent().getStringExtra(USERNAME_EXTRA_PARAMS));
    }


    void loadUserProfile(String username){
        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_USER_PROFILE_URL + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("LOaded user info", response);


                        Type typeToken = new TypeToken<UserDetail>(){}.getType();
                        UserDetail r = new Gson().fromJson(response,typeToken);



                        bindUserDetail(r);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
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
        tvNotes.setText(user.notes);

        Glide.with(this).load(user.avatarUrl).into(ivAvatar);
    }
}