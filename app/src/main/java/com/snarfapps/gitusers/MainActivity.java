package com.snarfapps.gitusers;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snarfapps.gitusers.db.AppDatabase;
import com.snarfapps.gitusers.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    NetworkQueue queue;
    int nextPageIndex = 0;

    List<User> users;

    RecyclerView rvUsers;
    ProgressBar pbLoading;

    private ArrayList<User> shimmerUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         Constants.db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "git-users-db").build();
         queue =  NetworkQueue.getInstance().setContext(getApplicationContext());


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

        adapter.setOnItemClickListener((user, position) -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class)
                    .putExtra(ProfileActivity.USERNAME_EXTRA_PARAMS,user.username));

        });
    }
    private boolean isLoading = false;
    private boolean isSearching = false;



    class InitiateDbTask extends AsyncTask<Void,Void, Void>{
        @Override
        protected void onPreExecute() {

            pbLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            users = Constants.db.userDao().getAllUsers();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isLoading = false;
            pbLoading.setVisibility(View.GONE);

            if(users.size() == 0){
                loadMoreUsers();
            }
            else{
                /**
                 * Data is loaded from db
                 */
                GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();
                adapter.setData(users);
                adapter.notifyDataSetChanged();
            }
        }
    }

    void searchUsers(final String key){
        if(isLoading) return; //disable search when the list is loading

        //Enable searching for both user id and username
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

        String url =  Constants.GET_USERS_URL +nextPageIndex;
        pbLoading.setVisibility(View.VISIBLE);
        isLoading = true;
        Log.e("Loading", "Loading from: "+nextPageIndex);

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            isLoading = false;
            pbLoading.setVisibility(View.GONE);

            Type typeToken = new TypeToken<ArrayList<User>>(){}.getType();
            ArrayList<User> r = new Gson().fromJson(response,typeToken);


            GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();

            users.addAll(r);
            //Set users as data source, in case the previous data
            //is the dummy users.
            adapter.setData(users);


            Log.e("New Users", "Added new users "+r.size()+ " First: "+r.get(0).username + " Last: "+ r.get(r.size()-1).username);

            //set since paramater for next batch of users from the last user id
            nextPageIndex = users.get(r.size()-1).id;
            adapter.notifyDataSetChanged();


            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... voids) {

                    Constants.db.userDao().insertAllUsers(users);
                    return null;
                }
            }.execute();


        }, error -> {
            Log.e("Volley err", error.getLocalizedMessage());
            isLoading = false;
            pbLoading.setVisibility(View.GONE);

        });

        queue.addQueue(request);
    }

}