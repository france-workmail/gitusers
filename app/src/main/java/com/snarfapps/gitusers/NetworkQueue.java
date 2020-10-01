package com.snarfapps.gitusers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetworkQueue {

    private static NetworkQueue instance = null;
    RequestQueue requestQueue;

    public static NetworkQueue getInstance(){

        if(instance ==null)
            instance = new NetworkQueue();
        else if(instance.requestQueue == null)
            throw new NullPointerException("Context must be set for an instance");

        return instance;
    }
    public void addQueue(StringRequest request){
        requestQueue.add(request);
    }
    public NetworkQueue setContext(Context ctx){
        requestQueue = Volley.newRequestQueue(ctx);
        return instance;
    }

}
