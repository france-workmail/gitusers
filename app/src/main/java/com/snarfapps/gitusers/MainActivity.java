package com.snarfapps.gitusers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snarfapps.gitusers.db.AppDatabase;
import com.snarfapps.gitusers.models.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    NetworkQueue queue;
    int nextPageIndex = 0;

    List<User> users;

    RecyclerView rvUsers;
    ProgressBar pbLoading;
    LinearLayout llLoadFailed;
    NetworkStateReceiver networkStateReceiver;

    public static GitUsersAdapter usersAdapter;

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
        usersAdapter = new GitUsersAdapter(shimmerUsers);

        rvUsers.setAdapter(usersAdapter);

        pbLoading = findViewById(R.id.pbLoading);

        llLoadFailed = findViewById(R.id.llLoadFailed);
        llLoadFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreUsers();
            }
        });

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
//                    GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();
                    usersAdapter.setData(users);
                    usersAdapter.notifyDataSetChanged();
                }

            }
        });

        usersAdapter.setOnItemClickListener((user, position) -> {
            startActivityForResult(new Intent(MainActivity.this, ProfileActivity.class)
                    .putExtra(ProfileActivity.USERNAME_EXTRA_PARAMS,user.username)
                    .putExtra(ProfileActivity.USERID_EXTRA_PARAMS, user.id) , 1);

        });


        /**
         *
         * Network state monitor
         *
         */

        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(new NetworkStateReceiver.NetworkStateReceiverListener() {
            @Override
            public void onNetworkAvailable() {
                Log.e("Sdada", "Network is available");
            }

            @Override
            public void onNetworkUnavailable() {

                Log.e("Sdada", "Network NOT available");
            }
        });

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
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

                nextPageIndex = users.get(users.size() -1).id;
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
//        GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();
        usersAdapter.setData(searchedUsers);
        usersAdapter.notifyDataSetChanged();
    }

    void loadMoreUsers(){
        if(isLoading)return;

        String url =  Constants.GET_USERS_URL +nextPageIndex;
        pbLoading.setVisibility(View.VISIBLE);
        isLoading = true;
        llLoadFailed.setVisibility(View.GONE);

        Log.e("Loading", "Loading from: "+nextPageIndex);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
            isLoading = false;
            pbLoading.setVisibility(View.GONE);

            Type typeToken = new TypeToken<ArrayList<User>>(){}.getType();
            ArrayList<User> r = new Gson().fromJson(response,typeToken);

            //Check if user already exists in the list
            // just to make sure we only have unique items
            //then add it so that we get an updated version of the
            //user data

            for(User u: r){
               List<User> existingUser =   users.stream().filter(i -> (
                       new String(""+i.id).equalsIgnoreCase(""+u.id)))
                       .collect(Collectors.toList());
               if(existingUser.size()>0){
                   Log.e("Exists", "User already exists: "+ existingUser.get(0).username);
                   //user already exists on list and db,
                   //remove it so that we'll get the most updated copy
                   //of the data
                   users.remove(existingUser.get(0));
               }
            }

            users.addAll(r);

//            GitUsersAdapter adapter = (GitUsersAdapter)rvUsers.getAdapter();

            //Set users as data source, in case the previous data
            //is the dummy users.
            usersAdapter.setData(users);


            //set since paramater for next batch of users from the last user id
            nextPageIndex = r.get(r.size()-1).id;
            usersAdapter.notifyDataSetChanged();

            updateUsers();
        },
                error -> {
            Log.e("Volley err", error!=null?error.getLocalizedMessage(): "Null error");
            isLoading = false;
            pbLoading.setVisibility(View.GONE);


            if(
                    error instanceof NetworkError ||
                    error instanceof ServerError ||
                    error instanceof AuthFailureError ||
                    error instanceof TimeoutError
                ){

                //start retry network check
//                isNetworkLost();
//                networkReachCheck();
            }

        });

        queue.addQueue(request);
    }
    @SuppressLint("StaticFieldLeak")
    void updateUsers(){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {

                Constants.db.userDao().insertAllUsers(users);
                return null;
            }
        }.execute();
    }
}