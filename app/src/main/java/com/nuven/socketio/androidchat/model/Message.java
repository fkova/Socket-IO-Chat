package com.nuven.socketio.androidchat.model;

public class Message {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mUsername;
    private String mSocketId;
    public boolean isMine=false;

    private Message() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };

    public String getSocketId() {
        return mSocketId;
    }

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private String mSocketId;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder socketId(String socketId){
            mSocketId =socketId;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.mSocketId=mSocketId;
            return message;
        }
    }
}
