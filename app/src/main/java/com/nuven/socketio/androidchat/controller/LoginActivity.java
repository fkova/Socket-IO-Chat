package com.nuven.socketio.androidchat.controller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nuven.socketio.androidchat.R;

import io.socket.client.Ack;
import io.socket.client.On;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity" ;
    private EditText mUsernameView;
    private String mUsername;
    private Socket mSocket;
    CallbackManager callbackManager;
    private ProfileTracker fbProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSocket.on("login", onLogin);

        LoginButton facebookLogin = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLogin.setReadPermissions("email");

        // Callback registration
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Facebook login Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(getApplicationContext(), "Facebook login Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(getApplicationContext(), "Facebook login Failed", Toast.LENGTH_SHORT).show();
            }
        });

        fbProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    attemptLoginFB(profile.getFirstName(),profile.getId());
                }
            }
        };

        if(MainActivity.uName!=null){
            mUsernameView.setText(MainActivity.uName);

            if(app.isNetworkConnected()){
                attemptLogin();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("login", onLogin);
    }

    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);

        String username = mUsernameView.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        mUsername = username;

        //save login name
        MainActivity.editor.putString("uName",username);
        MainActivity.editor.commit();


        mSocket.emit("add user", username,new Ack(){
            @Override
            public void call(Object... args) {
                if((boolean)args[0]){
                    //username is ok
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUsernameView.setError(getString(R.string.error_username_taken));
                            mUsernameView.requestFocus();
                        }
                    });
                }
            }
        });
    }

    private void attemptLoginFB(String fbName,String fbId) {
        mUsernameView.setError(null);

        mUsername = fbName;
        //Save login name
        MainActivity.editor.putString("uName",fbName);
        MainActivity.editor.commit();

        mSocket.emit("add fbuser", fbName,fbId);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject data = (JSONObject) args[0];

            int numUsers;
            JSONObject users;

            try {
                numUsers = data.getInt("numUsers");
                users = data.getJSONObject("userList");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            intent.putExtra("userList",users.toString());

            setResult(RESULT_OK, intent);
            mSocket.emit("get messages");
            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}



