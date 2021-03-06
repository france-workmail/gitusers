package com.snarfapps.gitusers;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        ArrayList<User> shimmerUsers = new ArrayList<>();
        for (int dummyCount =0 ; dummyCount < 15 ; dummyCount++){
            User u = new User();
            u.id = -1; // unique id to identify that the currently presented
                        // data on the recycler view is a dummy (See GitUsersAdapter, bind method to see its usage)
            shimmerUsers.add(u);
        }

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        //Set Recycler item customization
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        ShapeDrawable shapeDrawableForDivider = new ShapeDrawable(new RectShape());
        int dividerThickness = 10; // (int) (SomeOtherView.getHeight() * desiredPercent);
        shapeDrawableForDivider.setIntrinsicHeight(dividerThickness);
        shapeDrawableForDivider.setAlpha(0);
        dividerItemDecoration.setDrawable(shapeDrawableForDivider);
        rvUsers.addItemDecoration(dividerItemDecoration);




        //use dummy users for shimmering effect
        usersAdapter = new GitUsersAdapter(shimmerUsers);

        rvUsers.setAdapter(usersAdapter);

        pbLoading = findViewById(R.id.pbLoading);

        llLoadFailed = findViewById(R.id.llLoadFailed);
        llLoadFailed.setOnClickListener(v -> loadMoreUsers());




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
                    if(users.size() == 0) {
                        new InitiateDbTask().execute();
                    }
                    else{
                        usersAdapter.setData(users);
                        usersAdapter.notifyDataSetChanged();
                    }
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
                isNetworkAvailable = true;
                if(shouldRetry) {
                    Log.e("Connection", "Retrying...");
                    loadMoreUsers();
                }
            }

            @Override
            public void onNetworkUnavailable() {
                isNetworkAvailable = false;
            }
        });

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


//        Log.e("instance state","Instance state not null");
        if(savedInstanceState != null && savedInstanceState.getBoolean(IS_SEARCHING) ){
                String key = savedInstanceState.getString(SEARCH_KEY);
                scrollToPosition = savedInstanceState.getInt(LIST_POSITION);
                isFromOrientationChange = savedInstanceState.getBoolean(IS_SEARCH_FROM_ORIENTATION_CHANGE);

                Log.e("Search", "IsSearching: " + savedInstanceState.getBoolean(IS_SEARCHING));
                searchUsers(key);

        }
        else{
            if(users.size() == 0)
                new InitiateDbTask().execute();
            else
                loadMoreUsers();
        }


    }

    /**
     * Instance state keys constants
     */
    String IS_SEARCHING = "isSearching";
    String SEARCH_KEY = "searchKey";
    String LIST_POSITION = "adapterPosition";
    String IS_SEARCH_FROM_ORIENTATION_CHANGE = "isSearchFromOrientationChange";
    boolean isFromOrientationChange = false;
    int scrollToPosition = 0;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }
    private boolean isNetworkAvailable = false;

    //this variable is a flag to check once the
    //connection is regained.
    private boolean shouldRetry = false;

    private boolean isLoading = false;
    private boolean isSearching = false;



    @SuppressLint("StaticFieldLeak")
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
                usersAdapter.setData(users);
                usersAdapter.notifyDataSetChanged();

                nextPageIndex = users.get(users.size() -1).id;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    void searchUsers(final String key){
        if(isLoading) return; //disable search when the list is loading

//        //Enable searching for both user id and username
//        ArrayList<User> searchedUsers = (ArrayList<User>) users.stream().filter(
//                p -> (new String(p.id+"").contains(key) || p.username.contains(key)))
//                .collect(Collectors.toList());

        /**
         * Implement search using the local db
         */

        isSearching = true;
        new AsyncTask<Void,Void,List<User>>(){
            @Override
            protected List<User> doInBackground(Void... voids) {
                 return Constants.db.userDao().searchUserByNameOrNote("%"+key+"%");
            }

            @Override
            protected void onPostExecute(List<User> users) {
                super.onPostExecute(users);
                Log.e("Searched users", "Count: "+users.size());

                usersAdapter.setData(users);
                usersAdapter.notifyDataSetChanged();
                if(isFromOrientationChange){
                    rvUsers.scrollToPosition(Math.max(scrollToPosition, 0));
                }
            }
        }.execute();


    }




    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save search result
        outState.putBoolean(IS_SEARCHING, isSearching);
        if(isSearching){
            outState.putString(SEARCH_KEY, ((EditText)findViewById(R.id.etSearch)).getText().toString());
            outState.putInt(LIST_POSITION,((LinearLayoutManager) rvUsers.getLayoutManager()).findFirstVisibleItemPosition());
            outState.putBoolean(IS_SEARCH_FROM_ORIENTATION_CHANGE, true);
        }
    }

    void loadMoreUsers(){
        if(isLoading)return;

//        if(!isNetworkAvailable){
//            connectionLost();
//            return;
//        }


        shouldRetry = false;

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


            //Set users as data source, in case the previous data
            //is the dummy users.
            usersAdapter.setData(users);


            //set since parameter for next batch of users from the last user id
            nextPageIndex = r.get(r.size()-1).id;
            usersAdapter.notifyDataSetChanged();

            updateUsers();
        },
                error -> {
            Log.e("Volley err", error!=null? Objects.requireNonNull(error.getLocalizedMessage()) : "Null error");
            isLoading = false;
            pbLoading.setVisibility(View.GONE);
            shouldRetry = true;


            if(
                    error instanceof NetworkError ||
                    error instanceof ServerError ||
                    error instanceof AuthFailureError ||
                    error instanceof TimeoutError
                ){


                //TODO Network is lost, do some checks
                connectionLost();

            }

        });

        queue.addQueue(request);
    }

    void connectionLost(){
        pbLoading.setVisibility(View.GONE);
        llLoadFailed.setVisibility(View.VISIBLE);
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