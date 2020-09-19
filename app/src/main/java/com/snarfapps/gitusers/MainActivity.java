package com.snarfapps.gitusers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.snarfapps.gitusers.db.AppDatabase;
import com.snarfapps.gitusers.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "git-users-db").build();


        List<User> users =  db.userDao().getAllUsers();


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.github.com/users?since=0";


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

               for (int iterator = 0 ; iterator < response.length();iterator++){
                   JSONObject userObj;
                   try {
                        userObj = response.getJSONObject(iterator);
                   } catch (JSONException e) {
                       e.printStackTrace();
                       continue;
                   }

                   if(userObj!=null){
                       User u = new User();
                       try {
                           Log.e("New User", userObj.getString("login"));
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }

               }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });


        queue.add(request);
    }



}