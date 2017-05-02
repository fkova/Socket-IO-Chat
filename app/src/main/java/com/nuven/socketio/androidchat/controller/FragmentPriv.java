package com.nuven.socketio.androidchat.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nuven.socketio.androidchat.R;
import com.nuven.socketio.androidchat.adapter.MessageAdapter;
import com.nuven.socketio.androidchat.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FragmentPriv extends Fragment {

    private static final String TAG = "FragmentPriv";
    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private ImageButton sendButton;

    private List<Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    public static String mUsername;
    public String mPrivUsername;
    public String mPrivSocketId;
    public Socket mSocket;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MessageAdapter(getActivity(), mMessages);

        ChatApplication app = (ChatApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on("priv message", onPrivMessage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public static Fragment newInstance(String text) {
        FragmentPriv f = new FragmentPriv();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
        mInputMessageView.requestFocus();
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;
        if (mPrivUsername==null) return;

        String message = mInputMessageView.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        mSocket.emit("private message", message,mPrivSocketId,new Ack(){
            @Override
            public void call(Object... args) {
                if((boolean)args[0]){
                    //user online, msg sent
                }else{
                    //user offline
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"user offline",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void addMessage(String username, String message) {
        Message m=new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build();

        if (mUsername!=null)
            if(mUsername.equals(username)){
                m.isMine=true;
            }

        mMessages.add(m);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private void addLog(String message) {
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            if(ChatApplication.mPrivUsername!=null && mUsername==null){
                mUsername= ChatApplication.mUsername;
                mPrivUsername=ChatApplication.mPrivUsername;
                mPrivSocketId=ChatApplication.mPrivSocketId;
                addLog("Send to " + mPrivUsername);
            }
        }
        else {
        }
    }

    private Emitter.Listener onPrivMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    addMessage(username, message);
                }
            });
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("priv message", onPrivMessage);
    }
}
