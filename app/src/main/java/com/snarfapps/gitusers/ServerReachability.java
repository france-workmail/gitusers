package com.snarfapps.gitusers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerReachability {
    private boolean hasNetworkAvailable(Context context){
        String service = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(service);
        NetworkInfo network = manager.getActiveNetworkInfo();
        Log.d("ServerReachability", "hasNetworkAvailable: ${(network != null)}");
        return (network != null) && network.isConnected();
    }

    public boolean hasInternetConnected( Context context ) {
        if (hasNetworkAvailable(context)) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Constants.REACHABILITY_SERVER).openConnection();
                connection.setRequestProperty("User-Agent", "Test");
                connection.setRequestProperty("Connection", "close");
                connection.setConnectTimeout(1500);
                connection.connect();
                Log.d("ServerReachability", "hasInternetConnected: ${(connection.responseCode == 200)}");
                return (connection.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e("ServerReachability", "Error checking internet connection", e);
            }
        } else {
            Log.w("ServerReachability", "No network available!");
        }
        Log.d("ServerReachability", "hasInternetConnected: false");
        return false;
    }

    public boolean hasServerConnected(Context context) {
        if (hasNetworkAvailable(context)) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(Constants.REACHABILITY_SERVER).openConnection();
                connection.setRequestProperty("User-Agent", "Test");
                connection.setRequestProperty("Connection", "close");
                connection.setConnectTimeout(1500);
                connection.connect();
                Log.d("ServerReachability", "hasServerConnected: ${(connection.responseCode == 200)}");
                return (connection.getResponseCode() ==200);
            } catch (IOException e) {
                Log.e("ServerReachability", "Error checking internet connection", e);
            }
        } else {
            Log.w("ServerReachability", "Server is unavailable!");
        }
        Log.d("ServerReachability", "hasServerConnected: false");
        return false;
    }
}
