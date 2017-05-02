package com.nuven.socketio.androidchat.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.nuven.socketio.androidchat.adapter.MessageAdapter;
import com.nuven.socketio.androidchat.R;
import com.nuven.socketio.androidchat.model.ChatUser;
import com.nuven.socketio.androidchat.model.Message;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final int REQUEST_LOGIN = 0;
    private static final int TYPING_TIMER_LENGTH = 600;
    private static final String TAG = "MainFragment";

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;

    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    public String mUsername;
    public Socket mSocket;
    public int numUsers=0;

    public MainFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MessageAdapter(getActivity(), mMessages);
        ChatApplication app = (ChatApplication) getActivity().getApplication();

        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("load old msgs", onLoadOldMsgs);
        mSocket.connect();

        startSignIn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("load old msgs", onLoadOldMsgs);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            getActivity().finish();
            return;
        }

        mUsername = data.getStringExtra("username");
        numUsers = data.getIntExtra("numUsers", 1);

        JSONObject userList;

        try {
            userList=new JSONObject(data.getStringExtra("userList"));
            ChatUser s;
            for(int i = 0; i<userList.names().length(); i++){
                //Log.v(TAG, "key = " + userList.names().getString(i) + " value = " + userList.get(userList.names().getString(i)));
                s=new ChatUser(userList.names().getString(i), (String) userList.get(userList.names().getString(i)));
                ChatApplication.chatUsers.add(s);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ChatApplication.mUsername=mUsername;

        addLog(getResources().getString(R.string.message_welcome));
        addParticipantsLog(numUsers);
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addParticipantsLog(int numUsers) {
        getActivity().setTitle(getResources().getString(R.string.message_participants,numUsers));
    }

    private void addMessage(String username, String message,String socketId) {
        Message m=new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).socketId(socketId).build();

        if (mUsername!=null)
        if(mUsername.equals(username)){
            m.isMine=true;
        }

        mMessages.add(m);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message,mSocket.id());

        // perform the sending message attempt.
        mSocket.emit("new message", message);
    }

    public void startSignIn() {
        mUsername = null;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String socketId;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        socketId =data.getString("id");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);
                    addMessage(username, message,socketId);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String id;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                        id=data.getString("id");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);

                    ChatApplication.chatUsers.add(new ChatUser(username,id));
                    Log.d(TAG,"users:" + Integer.toString(ChatApplication.chatUsers.size()));
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String id;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                        id=data.getString("id");
                    } catch (JSONException e) {
                        return;
                    }

                    for(ChatUser cu : ChatApplication.chatUsers){
                        if(cu.id.equals(id)){
                            ChatApplication.chatUsers.remove(cu);
                            break;
                        }
                    }

                    Log.d(TAG,"users:" + Integer.toString(ChatApplication.chatUsers.size()));

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onLoadOldMsgs = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessages.clear(); //clear messages
                    mAdapter.notifyDataSetChanged();

                    JSONArray data = (JSONArray) args[0];
                    String username;
                    String message;
                    String socketId;

                    try {
                        for(int i=data.length()-1;i>=0;i--){
                            JSONObject obj= (JSONObject) data.get(i);
                            if(obj.getString("username")!=null && obj.getString("message")!=null){
                                username = obj.getString("username");
                                message = obj.getString("message");
                                socketId = obj.getString("socId");
                                addMessage(username, message,socketId);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };
}

