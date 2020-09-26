package com.snarfapps.gitusers;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

    ConstraintLayout clRoot;


    NetworkStateReceiver networkStateReceiver;
    LinearLayout llLoadFailed;

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

        clRoot = findViewById(R.id.clRoot);


        userName = getIntent().getStringExtra(USERNAME_EXTRA_PARAMS);
        userId  = getIntent().getIntExtra(USERID_EXTRA_PARAMS, -1);

        tvUsername.setText(userName);

        // Initially load the data from db
        loadFromDb();

        btnSave.setOnClickListener(v -> {

            etNotes.clearFocus();
            // Add the notes to user info.
            // then save the changes to db
            String notes = etNotes.getText().toString();
            if(!notes.isEmpty())
                userDetail.notes = notes;
            saveUserDetail();

            Toast.makeText(ProfileActivity.this,"Note saved!", Toast.LENGTH_SHORT).show();
        });
        ibBack.setOnClickListener(v -> finish());

        llLoadFailed = findViewById(R.id.llLoadFailed);
        llLoadFailed.setOnClickListener(v -> loadUserProfile(userName));

        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(new NetworkStateReceiver.NetworkStateReceiverListener() {
            @Override
            public void onNetworkAvailable() {
                if(shouldRetry) {
                    Log.e("Connection", "Retrying...");
                    loadUserProfile(userName);
                }
            }

            @Override
            public void onNetworkUnavailable() {
            }
        });

        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }
    //this variable is a flag to check once the
    //connection is regained.
    private boolean shouldRetry = false;


    void loadUserProfile(String username){

        shouldRetry = false;
        StringRequest request = new StringRequest(Request.Method.GET, Constants.GET_USER_PROFILE_URL + username,
                response -> {

                    Log.e("Fetch", "User detail fetched from server");

                    Type typeToken = new TypeToken<UserDetail>(){}.getType();


                    String notesHolder = null;
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


                    clRoot.setVisibility(View.VISIBLE);
                    llLoadFailed.setVisibility(View.GONE);

                },
                error -> {
                    shouldRetry = true;

                    if(!loadedFromDB) {
                        //dont show error if we have a local data available displayed
                        if(error.networkResponse!=null)
                        ((TextView) findViewById(R.id.tvErrorCode)).setText("" + error.networkResponse.statusCode);
                        connectionLost();
                    }
                });

        NetworkQueue.getInstance().addQueue(request);
    }

    void connectionLost(){
        clRoot.setVisibility(View.GONE);
        llLoadFailed.setVisibility(View.VISIBLE);
    }
    void bindUserDetail(UserDetail user){
        tvFollowers.setText("" + user.followers);
        tvFollowing.setText(""+user.following);
        tvName.setText("Name: " + user.name);
        tvCompany.setText("Company: "+ (user.company==null?"":user.company));
        tvBlog.setText("Blog: "+ (user.blog == null? "":user.blog));
        etNotes.setText(user.notes);

        Glide.with(this).load(user.avatarUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        /**
                         *
                         * This section is to generate a dominant color of the user image
                         * and set it as a gradient background of the layout
                         *
                         */


                        Palette p = Palette.from(( (BitmapDrawable)resource ).getBitmap()).generate();
                        int vibrantColor = p.getVibrantColor(Color.rgb(255,255,255));

                        ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory(){
                            @Override
                            public Shader resize(int width, int height) {
                                int[] colors = {vibrantColor, Color.BLACK};
                                float[] positions = {0.0f, 0.95f};
                                return new LinearGradient(0.0f,0.0f,0.0f,(float)clRoot.getHeight(),colors,positions,Shader.TileMode.REPEAT);
                            }
                        };


                        PaintDrawable paintDrawable = new PaintDrawable();
                        paintDrawable.setShape(new RectShape());
                        paintDrawable.setShaderFactory(shaderFactory);

                        clRoot.setBackground(paintDrawable);

//                        TransitionDrawable transitionDrawable = new TransitionDrawable( new Drawable[]{ new ColorDrawable(Color.WHITE),paintDrawable});
//                        clRoot.setBackground(transitionDrawable);
//                        transitionDrawable.startTransition(100);

                        return false;
                    }
                })

                .into(ivAvatar);
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
                    loadedFromDB = true;
                }

                //regardless if local data is present or not
                // we need to fetch from server to get an updated
                // copy of user data
                loadUserProfile(userName);

            }
        }.execute();
    }

    boolean loadedFromDB = false;

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