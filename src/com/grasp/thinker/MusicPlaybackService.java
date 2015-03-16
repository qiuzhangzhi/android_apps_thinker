package com.grasp.thinker;

import android.content.*;
import com.grasp.thinker.utils.PreferenceUtils;

import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by qiuzhangzhi on 15/1/11.
 */
public class MusicPlaybackService extends Service {

    private final static String TAG = "MusicPlaybackService" ;

    private final boolean DEBUG = false;

    private static final long REWIND_INSTEAD_PREVIOUS_THRESHOLD = 10000;

    public static final String SERVICECMD = "com.grasp.thinker.musicservicecommand";

    public static final String PAUSE_ACTION = "com.grasp.thinker.pause";

    public static final String STOP_ACTION = "com.grasp.thinker.stop";

    public static final String PREVIOUS_ACTION = "com.grasp.thinker.previous";

    public static final String NEXT_ACTION = "com.grasp.thinker.next";

    public static final String REPEAT_ACTION = "com.grasp.thinker.repeat";

    public static final String EXIT = "com.grasp.thinker.exit";

    public static final String TRACK_COMPLETE = "com.grasp.thinker.complete";

    public static final String FROM_MEDIA_BUTTON = "frommediabutton";

    public static final String PLAYSTATE_ALARM_CLOSE = "com.grasp.thinker.alarmclose";

    public static final String PLAYSTATE_CHANGED = "com.grasp.thinker.playstatechanged";

    public static final String META_CHANGED = "com.grasp.thinker.metachanged";

    public static final String TOGGLEPAUSE_ACTION = "com.grasp.thinker.togglepause";

    public static final String REFRESH = "com.grasp.thinker.refresh";

    public static final String CMDNAME = "command";

    public static final String CMDTOGGLEPAUSE = "togglepause";

    public static final String CMDSTOP = "stop";

    public static final String CMDPAUSE = "pause";

    public static final String CMDPLAY = "play";

    public static final String CMDPREVIOUS = "previous";

    public static final String CMDNEXT = "next";

    public static final String RECORD_POS ="record_pos";

    private boolean mIsSupposedToBePlaying = false;

    private static final int TRACK_WENT_TO_NEXT = 2;

    private static final int TRACK_ENDED = 1;

    private static final int IDCOLIDX = 0;

    private int mPlayListLen = 0;

    private int mPlayPos = -1;

    private int mNextPlayPos = -1;

    private long[] mPlayList = null;

    public static final int REPEAT_CURRENT = 1;

    public static final int REPEAT_ALL = 2;

    private static int mRepeatMode = REPEAT_ALL;

    private SharedPreferences mSharedPreferences;

    private ComponentName mMediaButtonReceiverComponent;

    private RemoteControlClient mRemoteControlClient;

    private String mFileToPlay;

    private Cursor mCursor;

    private MusicPlayerHandler mPlayerHandler;

    private MultiPlayer mPlayer;

    private NotificationHelper mNotificationHelper;

    private TelephonyManager mTelephonyManager;

    private AudioManager mAudioManager;

    private final IBinder mBinder = new ServiceStub(this);

    private static final String[] PROJECTION = new String[] {
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final HandlerThread thread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mSharedPreferences = getSharedPreferences("service",MODE_PRIVATE);
        mPlayerHandler = new MusicPlayerHandler(this, thread.getLooper());

        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        mNotificationHelper = new NotificationHelper(this);
        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

        reloadPosition();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){

            handleCommandIntent(intent);
        }

        return START_STICKY;

    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICECMD.equals(action) ? intent.getStringExtra(CMDNAME) : null;

        if (CMDNEXT.equals(command) || NEXT_ACTION.equals(action)) {
            gotoNext(true);
        } else if (CMDPREVIOUS.equals(command) || PREVIOUS_ACTION.equals(action)) {
            if (position() < REWIND_INSTEAD_PREVIOUS_THRESHOLD) {
                prev();
            } else {
                seek(0);
                play();
            }
        } else if (CMDTOGGLEPAUSE.equals(command) || TOGGLEPAUSE_ACTION.equals(action)) {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        } else if (CMDPAUSE.equals(command) || PAUSE_ACTION.equals(action)) {
            pause();
        } else if (CMDPLAY.equals(command)) {
            play();
        }else if(EXIT.equals(action)){
            if(DEBUG) Log.d(TAG,"EXIT BY ALARM");
            notifyChange(PLAYSTATE_ALARM_CLOSE);
            pause();
        }
    }

    public void stop() {
        stop(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPlayerHandler.removeCallbacksAndMessages(null);
        mPlayer.release();
        mPlayer = null;

        closeCursor();

    }


    public void play() {
        if (mPlayer.isInitialized()) {

            setNextTrack();

            final long duration = mPlayer.duration();
           if ( duration > 2000 && mPlayer.position() >= duration - 2000) {
                gotoNext(true);
            }

            mIsSupposedToBePlaying = true;

            mPlayer.start();

            notifyChange(META_CHANGED);
            notifyChange(PLAYSTATE_CHANGED);

            updateNotification();
        } else if (mPlayListLen <= 0) {

        }
    }


    public void pause() {
        synchronized (this) {
            mPlayer.pause();
            mIsSupposedToBePlaying = false;
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    public void gotoNext(final boolean force) {
        synchronized (this) {
            if (mPlayListLen <= 0) {
                return;
            }
            final int pos = getNextPosition();
            if (pos < 0) {
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }
                return;
            }
            mPlayPos = pos;
            stop(false);
            mPlayPos = pos;
            openCurrentAndNext();
            play();
            Log.d("playnext","gotoNext");
            notifyChange(META_CHANGED);
        }
    }

    public void prev() {
        synchronized (this) {
            if (mPlayListLen <= 0) {
                return;
            }
            if (mPlayPos > 0) {
                mPlayPos--;
            } else {
                mPlayPos = mPlayListLen - 1;
            }
            stop(false);

            openCurrentMaybeNext(true);

            play();
            notifyChange(META_CHANGED);
        }
    }


    private void stop(final boolean goToIdle) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
        closeCursor();

    }


    private boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    private long position(){
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    private long duration(){
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }
    private long seek(long position){
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            long result = mPlayer.seek(position);
            return result;
        }
        return -1;
    }

    private void refresh(long[] list){
        mPlayList = list;
        mPlayListLen = mPlayList.length;
        mPlayer.setNextDataSource(null);
        if(getTrackName() == null){
            long trackId = reloadPosition();
            for (int i = 0 ; i < list.length ; i++){
               if(list[i] == trackId ){
                   open(mPlayList,i);
                   notifyChange(META_CHANGED);
                   break;
               }
            }
        }
    }


    public void setRepeatMode(final int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            setNextTrack();
        }
    }

    public int getRepeatMode(){

        return mRepeatMode;
    }
    private boolean isInitialized(){
        synchronized (this){
            return mPlayer.isInitialized();
        }

    }


    private int getQueuePosition(){
        synchronized (this) {
            return mPlayPos;
        }
    }

    private long getSongId(){
        synchronized (this) {
            if (mCursor == null) {
                return 0;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID));
        }
    }
    private String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
        }
    }

    private String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));
        }
    }

    private String getArtistName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
        }
    }

    private void open(final long[] list, final int position){
        synchronized (this){
            mPlayList = list;
            mPlayListLen = mPlayList.length;
            mPlayPos = position;

            openCurrentAndNext();
        }

    }
    private boolean openFile(final String path){
        synchronized (this){
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                //  mOpenFailedCounter = 0;
                return true;
            }
            stop(true);
            return false;
        }
    }


    private void notifyChange(final String what){

        Intent intent = new Intent(what);
        if(TRACK_COMPLETE.equals(what)){
            intent.putExtra(RECORD_POS, mPlayList[mPlayPos]);
        }
        sendBroadcast(intent);
        if(META_CHANGED.equals(what)){
            savePosition(mPlayList[mPlayPos]);
        }else if(PLAYSTATE_CHANGED.equals(what)){
            mNotificationHelper.updatePlayState(isPlaying());
        }

    }

    private void setNextTrack() {
        mNextPlayPos = getNextPosition();
        if (mNextPlayPos >= 0 && mPlayList != null) {
            final long id = mPlayList[mNextPlayPos];
            mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
        } else {
            mPlayer.setNextDataSource(null);
        }
    }
    private int getNextPosition(){
        switch (mRepeatMode){
            default:
            case REPEAT_ALL:
                if (mPlayPos >= mPlayListLen - 1) {
                    return 0;
                } else {
                    return mPlayPos + 1;
                }
            case REPEAT_CURRENT:
                return mPlayPos;

        }

    }


    private void updateNotification(){
        mNotificationHelper.buildNotification(getAlbumName(), getArtistName(),
                getTrackName(), isPlaying());
    }
    private void openCurrentAndNext() {
        openCurrentMaybeNext(true);
    }

    private void openCurrentMaybeNext(boolean openNext){
        synchronized (this){
            updateCursor(mPlayList[mPlayPos]);

                if (mCursor != null && !mCursor.isClosed()
                        && openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                        + mCursor.getLong(IDCOLIDX))) {
                }else {
                    closeCursor();
                }
            if(openNext){
                setNextTrack();
            }
        }

    }


    private void updateCursor(final long trackId){
        updateCursor("_id=" + trackId, null);
    }

    private void updateCursor(final String selection, final String[] selectionArgs) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, selection, selectionArgs);
        }
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
            String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(uri, projection,
                selection, selectionArgs, null, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    private void savePosition(long trackId){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong("trackId",trackId);
        editor.commit();
    }

    private long reloadPosition(){
        return  mSharedPreferences.getLong("trackId",0);
    }

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){

        boolean isBreakByPhone = false;
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if(state == TelephonyManager.CALL_STATE_IDLE){
                if(isBreakByPhone){
                    Log.d(TAG,"CALL_STATE_IDLE,breakByPhone"+isBreakByPhone);
                    play();
                    isBreakByPhone = false;

                }
            }else {
                if(isPlaying()){
                    pause();
                    isBreakByPhone = true;
                    Log.d(TAG,"CALL_STATE_RINGING,breakByPhone"+isBreakByPhone);
                }
            }

        }
    };

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicPlaybackService> mService;

        public MusicPlayerHandler(final MusicPlaybackService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MusicPlaybackService>(service);
        }


        @Override
        public void handleMessage(final Message msg) {
            final MusicPlaybackService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case TRACK_WENT_TO_NEXT:
                    service.notifyChange(TRACK_COMPLETE);
                    service.mPlayPos = service.mNextPlayPos;
                    if (service.mCursor != null) {
                        service.mCursor.close();
                    }
                    service.updateCursor(service.mPlayList[service.mPlayPos]);
                    service.setNextTrack();
                    service.notifyChange(META_CHANGED);
                    service.updateNotification();
                    break;
                case TRACK_ENDED:
                    service.notifyChange(TRACK_COMPLETE);
                    service.gotoNext(false);
                    break;
            }
        }
    }


    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        private final WeakReference<MusicPlaybackService> mService;

        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

        private MediaPlayer mNextMediaPlayer;

        private Handler mHandler;

        private boolean mIsInitialized = false;

        public MultiPlayer(final MusicPlaybackService service) {
            mService = new WeakReference<MusicPlaybackService>(service);
        }

        public void setDataSource(final String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
            try {
                player.reset();
                player.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    player.setDataSource(mService.get(), Uri.parse(path));
                } else {
                    player.setDataSource(path);
                }
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
            } catch (final IOException todo) {
                // TODO: notify the user why the file couldn't be opened
                return false;
            } catch (final IllegalArgumentException todo) {
                // TODO: notify the user why the file couldn't be opened
                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);

            return true;
        }


        public void setNextDataSource(final String path) {
            try {
                mCurrentMediaPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "Next media player is current one, continuing");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Media player not initialized!");
                return;
            }
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }


        public void setHandler(final Handler handler) {
            mHandler = handler;
        }


        public boolean isInitialized() {
            return mIsInitialized;
        }


        public void start() {
            mCurrentMediaPlayer.start();
        }


        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }


        public void release() {
            stop();
            mCurrentMediaPlayer.release();
        }


        public void pause() {
            mCurrentMediaPlayer.pause();
        }


        private long duration() {
            return mCurrentMediaPlayer.getDuration();
        }


        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }


        public long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int)whereto);
            return whereto;
        }


        public void setVolume(final float vol) {
            mCurrentMediaPlayer.setVolume(vol, vol);
        }


        public void setAudioSessionId(final int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }


        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }


        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = new MediaPlayer();
                    return true;
                default:
                    break;
            }
            return false;
        }


        @Override
        public void onCompletion(final MediaPlayer mp) {
            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = mNextMediaPlayer;
                mNextMediaPlayer = null;
                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            } else {
                mHandler.sendEmptyMessage(TRACK_ENDED);
            }
        }
    }

    private static final class ServiceStub extends IThinkerService.Stub {

        private final WeakReference<MusicPlaybackService> mService;

        private ServiceStub(final MusicPlaybackService service) {
            mService = new WeakReference<MusicPlaybackService>(service);
        }


        @Override
        public void openFile(final String path) throws RemoteException {
            mService.get().openFile(path);
        }


        @Override
        public void open(final long[] list, final int position) throws RemoteException {
            mService.get().open(list, position);
        }


        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        @Override
        public void next() throws RemoteException {
            mService.get().gotoNext(true);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public long position() throws RemoteException {
            return mService.get().position();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public void seek(long pos) throws RemoteException {
              mService.get().seek(pos);
        }

        @Override
        public void refresh(long[] list) throws RemoteException {
            mService.get().refresh(list);
        }

        @Override
        public void setRepeatMode(int repeatmode) throws RemoteException {
            mService.get().setRepeatMode(repeatmode);
        }

        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().getRepeatMode();
        }

        @Override
        public boolean isInitialized() throws RemoteException {
            return mService.get().isInitialized();
        }


        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public long getSongId() throws RemoteException {
            return mService.get().getSongId();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }
    }
}
