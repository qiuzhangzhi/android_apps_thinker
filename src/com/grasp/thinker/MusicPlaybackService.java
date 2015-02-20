package com.grasp.thinker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
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

    private static final long REWIND_INSTEAD_PREVIOUS_THRESHOLD = 10000;

    public static final String SERVICECMD = "com.andrew.apollo.musicservicecommand";

    public static final String PAUSE_ACTION = "com.andrew.apollo.pause";

    /**
     * Called to go to stop the playback
     */
    public static final String STOP_ACTION = "com.andrew.apollo.stop";

    /**
     * Called to go to the previous track
     */
    public static final String PREVIOUS_ACTION = "com.andrew.apollo.previous";

    /**
     * Called to go to the next track
     */
    public static final String NEXT_ACTION = "com.andrew.apollo.next";

    /**
     * Called to change the repeat mode
     */
    public static final String REPEAT_ACTION = "com.andrew.apollo.repeat";


    public static final String FROM_MEDIA_BUTTON = "frommediabutton";

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

    private boolean mIsSupposedToBePlaying = false;

    private static final int TRACK_WENT_TO_NEXT = 2;

    private static final int TRACK_ENDED = 1;

    private static final int IDCOLIDX = 0;

    private int mPlayListLen = 0;

    private int mPlayPos = -1;

    private int mNextPlayPos = -1;

    private long[] mPlayList = null;

    /**
     * Repeats the current track in a list
     */
    public static final int REPEAT_CURRENT = 1;

    /**
     * Repeats all the tracks in a list
     */
    public static final int REPEAT_ALL = 2;

    private int mRepeatMode = REPEAT_ALL;

    private ComponentName mMediaButtonReceiverComponent;

    private RemoteControlClient mRemoteControlClient;
    /**
     * The path of the current file to play
     */
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

        // Initialize the handler
        mPlayerHandler = new MusicPlayerHandler(this, thread.getLooper());

        // Initialize the media player
        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);
/*

        final IntentFilter filter = new IntentFilter();
        filter.addAction(TOGGLEPAUSE_ACTION);
        registerReceiver(mIntentReceiver,filter);
*/
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        // Use the remote control APIs to set the playback state
       // setUpRemoteControlClient();


        mNotificationHelper = new NotificationHelper(this);
        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

    }
    /**
     * Initializes the remote control client
     */
    private void setUpRemoteControlClient() {
        final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);
        mRemoteControlClient = new RemoteControlClient(
                PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
        mAudioManager.registerRemoteControlClient(mRemoteControlClient);

        // Flags for the media transport control that this client supports.
        int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_STOP;


       /* if (ApolloUtils.hasJellyBeanMR2()) {
            flags |= RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE;

            mRemoteControlClient.setOnGetPlaybackPositionListener(
                    new RemoteControlClient.OnGetPlaybackPositionListener() {
                        @Override
                        public long onGetPlaybackPosition() {
                            return position();
                        }
                    });
            mRemoteControlClient.setPlaybackPositionUpdateListener(
                    new RemoteControlClient.OnPlaybackPositionUpdateListener() {
                        @Override
                        public void onPlaybackPositionUpdate(long newPositionMs) {
                            seek(newPositionMs);
                        }
                    });
        }*/


        mRemoteControlClient.setTransportControlFlags(flags);
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

        Log.d("playnext","handleCommandIntent");
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
        }
    }
    /**
     * Stops playback.
     */
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

        // Unregister the mount listener
      //  unregisterReceiver(mIntentReceiver);
    }

    /**
     * Resumes or starts playback.
     */
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

    /**
     * Temporarily pauses playback.
     */
    public void pause() {
       // if (D) Log.d(TAG, "Pausing playback");
        synchronized (this) {
            mPlayer.pause();
            mIsSupposedToBePlaying = false;
            notifyChange(PLAYSTATE_CHANGED);
          //  mPlayerHandler.removeMessages(FADEUP);
        /*    if (mIsSupposedToBePlaying) {
                mPlayer.pause();
                scheduleDelayedShutdown();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
            }*/
        }
    }

    /**
     * Changes from the current track to the next track
     */
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

    /**
     * Changes from the current track to the previous played track
     */
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


    /**
     * @return True if music is playing, false otherwise
     */
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
            //notifyChange(POSITION_CHANGED);
            return result;
        }
        return -1;
    }

    private void refresh(long[] list){
        mPlayList = list;
        mPlayListLen = mPlayList.length;
        mPlayer.setNextDataSource(null);
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
        sendBroadcast(intent);

        if(PLAYSTATE_CHANGED.equals(what)){
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
                // if we get here then opening the file failed. We can close the
                // cursor now, because
                // we're either going to create a new one next, or stop trying



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

/*    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        *//**
         * {@inheritDoc}
         *//*
        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();
            Log.d(TAG,"action:"+action);
            if(action.equals(TOGGLEPAUSE_ACTION)){
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }
        }
    };*/

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicPlaybackService> mService;
        private float mCurrentVolume = 1.0f;

        /**
         * Constructor of <code>MusicPlayerHandler</code>
         *
         * @param service The service to use.
         * @param looper The thread to run on.
         */
        public MusicPlayerHandler(final MusicPlaybackService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MusicPlaybackService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            final MusicPlaybackService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case TRACK_WENT_TO_NEXT:
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
                    service.gotoNext(false);
                    break;
        /*        case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .01f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (service.isPlaying()) {
                        service.gotoNext(true);
                    } else {
                        service.openCurrentAndNext();
                    }
                    break;
                case TRACK_WENT_TO_NEXT:
                    service.mPlayPos = service.mNextPlayPos;
                    if (service.mCursor != null) {
                        service.mCursor.close();
                    }
                    service.updateCursor(service.mPlayList[service.mPlayPos]);
                    service.notifyChange(META_CHANGED);
                    service.updateNotification();
                    service.setNextTrack();
                    break;
                case TRACK_ENDED:
                    if (service.mRepeatMode == REPEAT_CURRENT) {
                        service.seek(0);
                        service.play();
                    } else {
                        service.gotoNext(false);
                    }
                    break;
                case RELEASE_WAKELOCK:
                    service.mWakeLock.release();
                    break;
                case FOCUSCHANGE:
                    if (D) Log.d(TAG, "Received audio focus change event " + msg.arg1);
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (service.isPlaying()) {
                                service.mPausedByTransientLossOfFocus =
                                        msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                            }
                            service.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            removeMessages(FADEUP);
                            sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!service.isPlaying()
                                    && service.mPausedByTransientLossOfFocus) {
                                service.mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                service.mPlayer.setVolume(mCurrentVolume);
                                service.play();
                            } else {
                                removeMessages(FADEDOWN);
                                sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                    }
                    break;
                default:
                    break;*/
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

        /**
         * Constructor of <code>MultiPlayer</code>
         */
        public MultiPlayer(final MusicPlaybackService service) {
            mService = new WeakReference<MusicPlaybackService>(service);
        }

        /**
         * @param path The path of the file, or the http/rtsp URL of the stream
         *            you want to play
         */
        public void setDataSource(final String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        /**
         * @param player The {@link MediaPlayer} to use
         * @param path The path of the file, or the http/rtsp URL of the stream
         *            you want to play
         * @return True if the <code>player</code> has been prepared and is
         *         ready to play, false otherwise
         */
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

        /**
         * Set the MediaPlayer to start when this MediaPlayer finishes playback.
         *
         * @param path The path of the file, or the http/rtsp URL of the stream
         *            you want to play
         */
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

        /**
         * Sets the handler
         *
         * @param handler The handler to use
         */
        public void setHandler(final Handler handler) {
            mHandler = handler;
        }

        /**
         * @return True if the player is ready to go, false otherwise
         */
        public boolean isInitialized() {
            return mIsInitialized;
        }

        /**
         * Starts or resumes playback.
         */
        public void start() {
            mCurrentMediaPlayer.start();
        }

        /**
         * Resets the MediaPlayer to its uninitialized state.
         */
        public void stop() {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }

        /**
         * Releases resources associated with this MediaPlayer object.
         */
        public void release() {
            stop();
            mCurrentMediaPlayer.release();
        }

        /**
         * Pauses playback. Call start() to resume.
         */
        public void pause() {
            mCurrentMediaPlayer.pause();
        }

        /**
         * Gets the duration of the file.
         *
         * @return The duration in milliseconds
         */
        private long duration() {
            return mCurrentMediaPlayer.getDuration();
        }

        /**
         * Gets the current playback position.
         *
         * @return The current position in milliseconds
         */
        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }

        /**
         * Gets the current playback position.
         *
         * @param whereto The offset in milliseconds from the start to seek to
         * @return The offset in milliseconds from the start to seek to
         */
        public long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int)whereto);
            return whereto;
        }

        /**
         * Sets the volume on this player.
         *
         * @param vol Left and right volume scalar
         */
        public void setVolume(final float vol) {
            mCurrentMediaPlayer.setVolume(vol, vol);
        }

        /**
         * Sets the audio session ID.
         *
         * @param sessionId The audio session ID
         */
        public void setAudioSessionId(final int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }

        /**
         * Returns the audio session ID.
         *
         * @return The current audio session ID.
         */
        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = new MediaPlayer();
                    //mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
                   // mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                default:
                    break;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
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

        /**
         * {@inheritDoc}
         */
        @Override
        public void openFile(final String path) throws RemoteException {
            mService.get().openFile(path);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void open(final long[] list, final int position) throws RemoteException {
            mService.get().open(list, position);
        }

   /*     *//**
         * {@inheritDoc}
         *//*
        @Override
        public void stop() throws RemoteException {
            mService.get().stop(true);
        }*/

        /**
         * {@inheritDoc}
         */
        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        /**
         * {@inheritDoc}
         */
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
