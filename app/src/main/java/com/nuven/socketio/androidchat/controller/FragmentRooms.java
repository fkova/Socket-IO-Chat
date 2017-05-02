package com.nuven.socketio.androidchat.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuven.socketio.androidchat.R;

public class FragmentRooms extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment2, container, false);


        TextView tv = (TextView) v.findViewById(R.id.section_label);
        tv.setText(getArguments().getString("msg"));


        return v;
    }

    public static Fragment newInstance(String text) {
        FragmentRooms f = new FragmentRooms();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}
