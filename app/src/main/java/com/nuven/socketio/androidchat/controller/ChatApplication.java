package com.nuven.socketio.androidchat.controller;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.nuven.socketio.androidchat.model.ChatUser;
import com.nuven.socketio.androidchat.model.Constants;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatApplication extends Application {
    public static Uri link;
    public static String mUsername;
    public static String mPrivUsername;
    public static String mPrivSocketId;

    public static List<ChatUser> chatUsers= new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
