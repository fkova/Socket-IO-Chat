package com.nuven.socketio.androidchat.model;


public class ChatUser {
    public String id;
    public String name;

    public ChatUser(String name, String id) {
        this.name=name;
        this.id=id;
    }
}
