package com.nuven.socketio.androidchat.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.nuven.socketio.androidchat.R;
import com.nuven.socketio.androidchat.adapter.MessageAdapter;
import com.nuven.socketio.androidchat.adapter.MyPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private FragmentManager mSupportFM;
    public MyPagerAdapter mPagerAdapter;
    public ViewPager mViewPager;

    public static String uName;
    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp= getPreferences(MODE_PRIVATE);
        editor= sp.edit();
        uName=sp.getString("uName",null);

        mSupportFM=getSupportFragmentManager();
        mPagerAdapter = new MyPagerAdapter(mSupportFM);

        mViewPager = (ViewPager) findViewById(R.id.pager_container);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch(position) {
                    case 0:
                        MainFragment frag=(MainFragment) getActiveFragment(mViewPager,0);
                        setTitle(getResources().getString(R.string.message_participants,frag.numUsers));
                        break;
                    case 1:
                        setTitle("Private Chat/Mailbox");
                        break;
                    case 2:
                        setTitle("Chat Rooms");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public Fragment getActiveFragment(ViewPager container, int position) {
        String name = makeFragmentName(container.getId(), position);
        return  mSupportFM.findFragmentByTag(name);
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_leave:
                ChatApplication app = (ChatApplication) getApplication();
                app.getSocket().disconnect();
                finish();
                break;
            case R.id.action_logout:
                mViewPager.setCurrentItem(0);

                LoginManager.getInstance().logOut();
                MainFragment frag=(MainFragment) getActiveFragment(mViewPager,0);
                frag.mUsername = null;
                uName=null;
                frag.mSocket.disconnect();
                frag.mSocket.connect();
                frag.startSignIn();
                ChatApplication.chatUsers.clear();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
