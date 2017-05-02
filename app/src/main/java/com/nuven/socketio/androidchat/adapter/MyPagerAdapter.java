package com.nuven.socketio.androidchat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.nuven.socketio.androidchat.controller.FragmentPriv;
import com.nuven.socketio.androidchat.controller.FragmentRooms;
import com.nuven.socketio.androidchat.controller.MainFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return new MainFragment();
            case 1: return new FragmentPriv();
            case 2: return FragmentRooms.newInstance("ThirdFragment");
            default: return new MainFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}
