package com.nuven.socketio.androidchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nuven.socketio.androidchat.R;
import com.nuven.socketio.androidchat.controller.ChatApplication;
import com.nuven.socketio.androidchat.controller.FragmentPriv;
import com.nuven.socketio.androidchat.controller.MainActivity;
import com.nuven.socketio.androidchat.controller.MainFragment;
import com.nuven.socketio.androidchat.model.Message;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private int[] mUsernameColors;
    private Context caller;

    Message message;

    public MessageAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
        caller=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
        case Message.TYPE_MESSAGE:
            layout = R.layout.item_message;
            break;
        case Message.TYPE_LOG:
            layout = R.layout.item_log;
            break;
        case Message.TYPE_ACTION:
            layout = R.layout.item_action;
            break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        message = mMessages.get(position);
        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsername());
        viewHolder.msg=message;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private static final String TAG ="ViewHolder" ;
        private TextView mUsernameView;
        private TextView mMessageView;
        private LinearLayout parent_layout;
        private LinearLayout layout;
        public Message msg;

        public ViewHolder(final View itemView) {
            super(itemView);

            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            parent_layout = (LinearLayout) itemView.findViewById(R.id.bubble_layout_parent);
            layout=(LinearLayout) itemView.findViewById(R.id.bubble_layout);

        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;

            mUsernameView.setText(username);
            mUsernameView.setTextColor(getUsernameColor(username));

            if(username!=ChatApplication.mUsername){
                mUsernameView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
                mUsernameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goPrivat();
                    }
                });
            }


            if (parent_layout!=null && layout!=null){
                if (message.isMine){
                    parent_layout.setGravity(Gravity.RIGHT);
                    layout.setBackgroundResource(R.drawable.bubble2);
                }else{
                    parent_layout.setGravity(Gravity.LEFT);
                    layout.setBackgroundResource(R.drawable.bubble1);
                }

            }
        }

        private void goPrivat() {
            MainActivity mf = (MainActivity) caller;
            FragmentPriv.mUsername = null;
            ChatApplication.mPrivUsername = msg.getUsername();
            ChatApplication.mPrivSocketId = msg.getSocketId();
            mf.mViewPager.setCurrentItem(1);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
        }

        public void setId(String socketId) {
            ChatApplication.mPrivSocketId=socketId;
        }

        private int getUsernameColor(String username) {
            int hash = 7;
            for (int i = 0, len = username.length(); i < len; i++) {
                hash = username.codePointAt(i) + (hash << 5) - hash;
            }
            int index = Math.abs(hash % mUsernameColors.length);
            return mUsernameColors[index];
        }

        private final View.OnCreateContextMenuListener mOnCreateContextMenuListener = new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (message!= null) {
                    menu.add(0, 1, 0, "Send Privat Message").setOnMenuItemClickListener(mOnMyActionClickListener);
                    menu.add(0, 2, 0, "Show Facebook Profil").setOnMenuItemClickListener(mOnMyActionClickListener);
                }
            }
        };

        private final MenuItem.OnMenuItemClickListener mOnMyActionClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MainActivity mf= (MainActivity) caller;

                switch (item.getItemId()) {
                    case 1:
                        goPrivat();
                        break;
                    case 2:
                        if(ChatApplication.link!=null && !ChatApplication.link.equals(Uri.EMPTY)){
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, ChatApplication.link);
                            itemView.getContext().startActivity(browserIntent);
                        }else{
                            Toast.makeText(mf,"No Facebook Profil avilable to " +msg.getUsername(),Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        };


    }
}
