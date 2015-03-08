package com.grasp.thinker.ui.activitys;

import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerApplication;
import com.grasp.thinker.adapters.PageAdapter;
import com.grasp.thinker.interfaces.ColorObserver;
import com.grasp.thinker.ui.EnumFragment;
import com.grasp.thinker.ui.fragmens.AlarmDialogFragment;
import com.grasp.thinker.utils.MusicUtils;
import com.grasp.thinker.utils.ThinkerUtils;
import com.grasp.thinker.widgets.ColorSchemeDialog;
import com.grasp.thinker.widgets.PlayPauseButton;
import com.grasp.thinker.widgets.RepeatingImageButton;
import com.grasp.thinker.widgets.theme.ThemeableSeekBar;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by qiuzhangzhi on 15/1/4.
 */
public class HomeActivity extends FragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener,
        ServiceConnection,ColorObserver{

    private final static boolean DEBUG = true;

    private final static String TAG = "HomeActivity";

    private final static int LOCAL_MUSIC = 0;

    private final static int SETTING = 1;

    private static final int REFRESH_TIME = 1;

    private long mPosOverride = -1;

    private long mStartSeekPos = 0;

    private long mLastSeekEventTime;

    private long mLastShortSeekEventTime;

    private TimeHandler mTimeHandler;

    private PlaybackStatus mPlaybackStatus;

    private ActionBar mActionBar;

    private View mCustomActionBarView;

    private ViewPager mViewPager;

    private PageAdapter mPageAdapter;

    private TextView mActionBarLocalMusic;

    private TextView mActionBarSetting;

    //底部 actionbar
    private View mBABContent;

    private TextView mBABTrackName;

    private TextView mBABAlbumInfo;

    private PlayPauseButton mBABPlayPauseButton;

    private PopupWindow mPlayBackPopupWindow;

    private View mPopupWindowContent;

    private TextView mPopupSongInfo;

    private TextView mPopupTimeProgress;

    private TextView mPopupTimeDuration;

    private ThemeableSeekBar mPopupSeekBar;

    private PlayPauseButton mPopupPlayPauseButton;

    private RepeatingImageButton mPopupPreviousButton;

    private RepeatingImageButton mPopupNextButton;

    private ImageButton mPopupAlarmButton;

    private MusicUtils.ServiceToken mToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        findViews();
        initPupUpWindow();
        setListener();

    }

    @Override
    protected void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.META_CHANGED);
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        filter.addAction(MusicPlaybackService.PLAYSTATE_ALARM_CLOSE);
        registerReceiver(mPlaybackStatus, filter);

        final long next = refreshCurrentTime();

        queueNextRefresh(next);
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateBotomActionBarInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mPlayBackPopupWindow.isShowing()){
            mPlayBackPopupWindow.dismiss();
        }
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
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        mActionBar.setCustomView(mCustomActionBarView);
        mActionBar.setBackgroundDrawable(new ColorDrawable(ThinkerApplication.mThemeColor));

        // Control the media volume
      //  setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Bind Thinker's service
        mToken = MusicUtils.bindToService(this, this);

        mPlaybackStatus = new PlaybackStatus(this);
        mTimeHandler = new TimeHandler(this);

        ColorSchemeDialog.addObserver(this);
     /*   sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(
                "file://" + Environment.getExternalStorageDirectory())));*/
     /*   File file = new File(Environment.getExternalStorageDirectory(),"netease");
        Intent intent =
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);*/
    }
    private void initPupUpWindow(){
        mPopupWindowContent = LayoutInflater.from(this).inflate(R.layout.popup_playback,null);
        mPopupSongInfo = (TextView)mPopupWindowContent.findViewById(R.id.popup_song_info);
        mPopupTimeProgress = (TextView)mPopupWindowContent.findViewById(R.id.popup_song_time_progress);
        mPopupTimeDuration = (TextView)mPopupWindowContent.findViewById(R.id.popup_song_time_duration);
        mPopupSeekBar = (ThemeableSeekBar)mPopupWindowContent.findViewById(R.id.progress);
        mPopupPlayPauseButton = (PlayPauseButton)mPopupWindowContent.findViewById(R.id.popup_button_play);
        mPopupPreviousButton = (RepeatingImageButton)mPopupWindowContent.findViewById(R.id.popup_button_previous);
        mPopupNextButton =(RepeatingImageButton)mPopupWindowContent.findViewById(R.id.popup_button_next);
        mPopupAlarmButton = (ImageButton)mPopupWindowContent.findViewById(R.id.popup_alarm);


        mPlayBackPopupWindow = new PopupWindow(mPopupWindowContent, ViewGroup.LayoutParams.MATCH_PARENT,
                ThinkerUtils.dp2px(this,100),true);
        mPlayBackPopupWindow.setOutsideTouchable(true);
        mPlayBackPopupWindow.setBackgroundDrawable(new ColorDrawable(R.color.white));
        mPlayBackPopupWindow.setAnimationStyle(R.style.PopupAnimation);
        mPopupAlarmButton.setOnClickListener(this);

    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        updateBotomActionBarInfo();
        updateBottomPopupInfo();
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
              //  mActionBarLocalMusic.setBackgroundResource(R.color.actionbar_pressed_background);
                //mActionBarSetting.setBackgroundResource(R.color.transparent);
                break;
            case SETTING:
               // mActionBarSetting.setBackgroundResource(R.color.actionbar_pressed_background);
               // mActionBarLocalMusic.setBackgroundResource(R.color.transparent);
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
            case R.id.actionbar_bottom:
                mPlayBackPopupWindow.showAtLocation(mBABContent, Gravity.BOTTOM,0,0);
                break;
            case R.id.popup_alarm:
                AlarmDialogFragment alarmDialog = new AlarmDialogFragment();
                alarmDialog.show(getSupportFragmentManager(),"alarmDialog");
                break;
        }
    }

    private void updateBotomActionBarInfo(){
        mBABTrackName.setText(MusicUtils.getTrackName());
        mBABAlbumInfo.setText(String.format(getString(R.string.divider_artist_album),
                MusicUtils.getArtistName(),MusicUtils.getAlbumName()));
        mBABPlayPauseButton.updateState();
    }

    private void updateBottomPopupInfo(){
        mPopupSongInfo.setText(String.format(getString(R.string.divider_artist_album),MusicUtils.getTrackName(),MusicUtils.getArtistName()));
        mPopupTimeDuration.setText(MusicUtils.makeTimeString(this, MusicUtils.duration()/1000));
        mPopupPlayPauseButton.updateState();
    }

    private void refreshCurrentTimeText(final long pos){
        mPopupTimeProgress.setText(MusicUtils.makeTimeString(this, pos/1000));
    }
    private long refreshCurrentTime(){

      final long pos = mPosOverride < 0 ? MusicUtils.position() : mPosOverride;

        if(pos >= 0 && MusicUtils.duration() > 0){
            refreshCurrentTimeText(pos);
            final int progress = (int)(1000 * pos / MusicUtils.duration());
            mPopupSeekBar.setProgress(progress);
        }

        final long remaining = 1000 - pos % 1000;

        return remaining;
    }

    private void scanForward(final int repcnt, long delta) {

        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos + delta;
            final long duration = MusicUtils.duration();
            if (newpos >= duration) {
                // move to next track
                MusicUtils.next();
                mStartSeekPos -= duration; // is OK to go negative
                newpos -= duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }

    private void scanBackward(final int repcnt, long delta) {

        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos - delta;
            if (newpos < 0) {
                // move to previous track
                MusicUtils.previous();
                final long duration = MusicUtils.duration();
                mStartSeekPos += duration;
                newpos += duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }

    private void queueNextRefresh(final long delay) {
            final Message message = mTimeHandler.obtainMessage(REFRESH_TIME);
            mTimeHandler.removeMessages(REFRESH_TIME);
            mTimeHandler.sendMessageDelayed(message, delay);
    }

    private static final class TimeHandler extends Handler {

        private final WeakReference<HomeActivity> homeActivity;

        /**
         * Constructor of <code>TimeHandler</code>
         */
        public TimeHandler(final HomeActivity activity) {
            homeActivity = new WeakReference<HomeActivity>(activity);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case REFRESH_TIME:
                    final long next = homeActivity.get().refreshCurrentTime();

                    if(DEBUG) Log.d(TAG,""+next);
                    homeActivity.get().queueNextRefresh(next);
                    break;
                default:
                    break;
            }
        }
    }

    private void findViews(){
        mActionBarLocalMusic = (TextView)mCustomActionBarView.findViewById(R.id.action_button_local);
        mActionBarSetting = (TextView)mCustomActionBarView.findViewById(R.id.action_button_setting);

        mBABTrackName = (TextView)findViewById(R.id.bottom_action_bar_line_one);
        mBABAlbumInfo = (TextView)findViewById(R.id.bottom_action_bar_line_two);
        mBABPlayPauseButton = (PlayPauseButton)findViewById(R.id.action_button_play);

        mBABContent =findViewById(R.id.actionbar_bottom);

    }

    private void setListener(){
        mViewPager.setOnPageChangeListener(this);

        mActionBarLocalMusic.setOnClickListener(this);
        mActionBarSetting.setOnClickListener(this);
        mBABContent.setOnClickListener(this);

        mPopupNextButton.setRepeatListener(mFastForwardListener);
        mPopupPreviousButton.setRepeatListener(mRewindListener);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_home_actions, menu);
     /*   MenuItem searchItem = menu.findItem(R.id.action_setting);
        ImageView searchView = (ImageView) searchItem.getActionView();
        searchView.setBackgroundResource(R.drawable.selector_actionbar_button);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });*/
        return super.onCreateOptionsMenu(menu);
    }


    private final RepeatingImageButton.RepeatListener mFastForwardListener = new RepeatingImageButton.RepeatListener() {
        @Override
        public void onRepeat(View v, long duration, int repeatcount) {
            scanForward(repeatcount,duration);
        }
    };

    private final RepeatingImageButton.RepeatListener mRewindListener = new RepeatingImageButton.RepeatListener() {
        @Override
        public void onRepeat(View v, long duration, int repeatcount) {
            scanBackward(repeatcount,duration);
        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:


                return false;
            default:
                return super.onOptionsItemSelected(item);
        }

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
                mReference.get().mPopupPlayPauseButton.updateState();
            }else if(action.equals(MusicPlaybackService.META_CHANGED)){
                mReference.get().updateBotomActionBarInfo();
                mReference.get().updateBottomPopupInfo();
            }else if(action.equals(MusicPlaybackService.PLAYSTATE_ALARM_CLOSE)){
                ThinkerApplication.mAlarmWhich = 0 ;
            }
        }


    }

    @Override
    public void updateColor(int color) {
        if (DEBUG) Log.d(TAG,"updateColor");
        mActionBar.setBackgroundDrawable(new ColorDrawable(color));
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
    }
}

