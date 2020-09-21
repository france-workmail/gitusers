package com.snarfapps.gitusers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snarfapps.gitusers.db.AppDatabase;
import com.snarfapps.gitusers.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    RequestQueue queue;
    int nextPageIndex = 0;

    List<User> users;
    AppDatabase db;

    RecyclerView rvUsers;
    ProgressBar pbLoading;

    private ArrayList<User> shimmerUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "git-users-db").build();
         queue = Volley.newRequestQueue(this);


        /**
         * Setup recyclerview
         */

        users = new ArrayList<>();

        /**
         * The following sets the shimmer skeleton of
         * the recyclerview
         */
        shimmerUsers = new ArrayList<>();
        for (int dummyCount =0 ; dummyCount < 15 ; dummyCount++){
            User u = new User();
            u.id = -1; // unique id to identify that the currently presented
                        // data on the recycler view is a dummy (See GitUsersAdapter, bind method to see its usage)
            shimmerUsers.add(u);
        }

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        //use dummy users for shimmering effect
        final GitUsersAdapter adapter = new GitUsersAdapter(shimmerUsers);

        rvUsers.setAdapter(adapter);

        pbLoading = findViewById(R.id.pbLoading);

        if(users.size() == 0)
            new InitiateDbTask().execute();
        else
            loadMoreUsers();


        rvUsers.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) rvUsers.getLayoutManager();

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isSearching) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreUsers();
                    }
                }
            }
        });

        ((EditText)findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.length() != 0) {
                    searchUsers(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length() == 0) {
                    isSearching = false;
                    //return the data set
                    GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();
                    adapter.setData(users);
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }
    private boolean isLoading = false;
    private boolean isSearching = false;





    class InitiateDbTask extends AsyncTask<Void,Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            users = db.userDao().getAllUsers();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadMoreUsers();
        }
    }

    void searchUsers(final String key){
        if(isLoading) return;

        ArrayList<User> searchedUsers = (ArrayList<User>) users.stream().filter(
                p -> (new String(p.id+"").contains(key) || p.username.contains(key)))
                .collect(Collectors.toList());

        Log.e("Searched users", "Count: "+searchedUsers.size());

        isSearching = true;
        GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();
        adapter.setData(searchedUsers);
        adapter.notifyDataSetChanged();
    }

    void loadMoreUsers(){

        String url = "https://api.github.com/users?since="+nextPageIndex;
        pbLoading.setVisibility(View.VISIBLE);
        isLoading = true;
        Log.e("Loading", "Loading from: "+nextPageIndex);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                isLoading = false;
                pbLoading.setVisibility(View.GONE);

                Type typeToken = new TypeToken<ArrayList<User>>(){}.getType();
                ArrayList<User> r = new Gson().fromJson(response,typeToken);


                GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();

                users.addAll(r);
                adapter.setData(users);


                Log.e("New Users", "Added new users "+r.size()+ " First: "+r.get(0).username + " Last: "+ r.get(r.size()-1).username);

                //get since paramater for next batch of users
                nextPageIndex = r.get(r.size()-1).id;
                adapter.notifyDataSetChanged();



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley err", error.getLocalizedMessage());
                isLoading = false;
                pbLoading.setVisibility(View.GONE);

            }
        });
        queue.add(request);
    }

}