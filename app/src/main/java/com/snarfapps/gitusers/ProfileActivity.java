package com.snarfapps.gitusers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class ProfileActivity extends AppCompatActivity {

    public static String USERNAME_EXTRA_PARAMS = "username";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        loadUserProfile(getIntent().getStringExtra(USERNAME_EXTRA_PARAMS));
    }


    void loadUserProfile(String username){
        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_USER_PROFILE_URL + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("LOaded user info", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
            });

        NetworkQueue.getInstance().addQueue(request);
    }
}