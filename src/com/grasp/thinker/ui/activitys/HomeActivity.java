package com.grasp.thinker.ui.activitys;

import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.adapters.PageAdapter;
import com.grasp.thinker.ui.EnumFragment;
import com.grasp.thinker.utils.MusicUtils;
import com.grasp.thinker.widgets.PlayPauseButton;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by qiuzhangzhi on 15/1/4.
 */
public class HomeActivity extends FragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener,
        ServiceConnection{

    private final static int LOCAL_MUSIC = 0;

    private final static int SETTING = 1;

    private PlaybackStatus mPlaybackStatus;

    private ActionBar mActionBar;

    private View mCustomActionBarView;

    private ViewPager mViewPager;

    private PageAdapter mPageAdapter;

    private TextView mActionBarLocalMusic;

    private TextView mActionBarSetting;

    //底部 actionbar
    private TextView mBABTrackName;

    private TextView mBABAlbumInfo;

    private PlayPauseButton mBABPlayPauseButton;

    private MusicUtils.ServiceToken mToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        findViews();
        setListener();

    }

    @Override
    protected void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.META_CHANGED);
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        registerReceiver(mPlaybackStatus,filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBotomActionBarInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mToken != null) {
            MusicUtils.unbindFromService(mToken);
            mToken = null;
        }

        // Unregister the receiver
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {
            //$FALL-THROUGH$
        }

        // Remove any music status listeners
     //   mMusicStateListener.clear();
    }

    private void init(){

        mCustomActionBarView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.actionbar_custom,null);

        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mPageAdapter = new PageAdapter(this);
        final EnumFragment[] mFragments = EnumFragment.values();
        for(final EnumFragment fragment : mFragments){
            mPageAdapter.add(fragment.getFragmentClass(),null);
        }

        mViewPager.setAdapter(mPageAdapter);

        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled (false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        mActionBar.setCustomView(mCustomActionBarView);


        // Control the media volume
      //  setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Bind Thinker's service
        mToken = MusicUtils.bindToService(this, this);

        mPlaybackStatus = new PlaybackStatus(this);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        updateBotomActionBarInfo();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        switch (i){
            case LOCAL_MUSIC:
                mActionBarLocalMusic.setBackgroundResource(R.color.actionbar_pressed_background);
                mActionBarSetting.setBackgroundResource(R.color.transparent);
                break;
            case SETTING:
                mActionBarSetting.setBackgroundResource(R.color.actionbar_pressed_background);
                mActionBarLocalMusic.setBackgroundResource(R.color.transparent);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_button_local:
                mViewPager.setCurrentItem(LOCAL_MUSIC);
                break;
            case R.id.action_button_setting:
                mViewPager.setCurrentItem(SETTING);
                break;
        }
    }

    private void updateBotomActionBarInfo(){
        mBABTrackName.setText(MusicUtils.getTrackName());
        mBABAlbumInfo.setText(String.format(getString(R.string.divider_artist_album),
                MusicUtils.getArtistName(),MusicUtils.getAlbumName()));
        mBABPlayPauseButton.updateState();
    }

    private void findViews(){
        mActionBarLocalMusic = (TextView)mCustomActionBarView.findViewById(R.id.action_button_local);
        mActionBarSetting = (TextView)mCustomActionBarView.findViewById(R.id.action_button_setting);

        mBABTrackName = (TextView)findViewById(R.id.bottom_action_bar_line_one);
        mBABAlbumInfo = (TextView)findViewById(R.id.bottom_action_bar_line_two);
        mBABPlayPauseButton = (PlayPauseButton)findViewById(R.id.action_button_play);


    }

    private void setListener(){
        mViewPager.setOnPageChangeListener(this);

        mActionBarLocalMusic.setOnClickListener(this);
        mActionBarSetting.setOnClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        return super.onCreateOptionsMenu(menu);
    }


    private static final class PlaybackStatus extends BroadcastReceiver{

        private final WeakReference<HomeActivity> mReference;

        public PlaybackStatus(final HomeActivity activity) {
            mReference = new WeakReference<HomeActivity>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if(action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)){
                mReference.get().mBABPlayPauseButton.updateState();
            }else if(action.equals(MusicPlaybackService.META_CHANGED)){
                mReference.get().updateBotomActionBarInfo();
            }
        }
    }
}