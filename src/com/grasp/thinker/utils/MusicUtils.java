package com.grasp.thinker.utils;

import com.grasp.thinker.IThinkerService;
import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.adapters.SongAdapter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.WeakHashMap;

/**
 * Created by qiuzhangzhi on 15/1/11.
 */
public class MusicUtils {

    public static IThinkerService mService = null;

    private static final long[] sEmptyList = null;

    private static final WeakHashMap<Context , ServiceBinder> mConnectionMap;

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
    }

    public static final ServiceToken bindToService(final Context context,
            final ServiceConnection callback) {
        Activity realActivity = ((Activity)context).getParent();
        if (realActivity == null) {
            realActivity = (Activity)context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);

        contextWrapper.startService(new Intent(contextWrapper, MusicPlaybackService.class));

        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicPlaybackService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;


        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IThinkerService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        /**
         * Constructor of <code>ServiceToken</code>
         *
         * @param context The {@link ContextWrapper} to use
         */
        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }


    /**
     * Plays or pauses the music.
     */
    public static void playOrPause() {
        try {
            if (mService != null && mService.isInitialized()) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (final Exception ignored) {
        }
    }

    public static final boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    public static final long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (final RemoteException ignored) {
        }
    }
    
    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static void previous(){
        try {
            if (mService != null) {
                mService.prev();
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void cycleRepeat() {
        try {
            if (mService != null) {
                switch (mService.getRepeatMode()) {
                    case MusicPlaybackService.REPEAT_ALL:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_CURRENT);
                        break;
                    case MusicPlaybackService.REPEAT_CURRENT:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static final int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    /**
     * @return The current artist name.
     */
    public static final String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    /**
     * @return The current album name.
     */
    public static final String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getAlbumName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final void refresh(SongAdapter adapter){
        try {
            if (mService != null) {
                final long[] list = MusicUtils.getSongListForAdapter(adapter);
                mService.refresh(list);
            }
        } catch (final RemoteException ignored) {
        }

    }
    /**
     * @param context The {@link Context} to use.
     * @param list The list of songs to play.
     * @param position Specify where to start.
     * @param forceShuffle True to force a shuffle, false otherwise.
     */
    public static void playAll(final Context context, final long[] list, int position,
            final boolean forceShuffle) {
        if (list.length == 0 || mService == null) {
            return;
        }
        try {
            if(list[position] == mService.getSongId()){
                   playOrPause();
            }else{
                mService.open(list,position);
                mService.play();
            }

        } catch (final RemoteException ignored) {
        }
    }

    public static void playAllFromUserItemClick(final Context context,
            SongAdapter adapter, final int position) {


        final long[] list = MusicUtils.getSongListForAdapter(adapter);
        int pos = position;
        if (list.length == 0) {
            pos = 0;
        }
        MusicUtils.playAll(context, list, pos, false);
    }

    private static final long[] getSongListForAdapter(SongAdapter adapter) {
        if (adapter == null) {
            return sEmptyList;
        }
        long[] list = {};
        if (adapter != null) {
            int count = adapter.getCount();
            list = new long[count];
            for (int i = 0; i < count; i++) {
                list[i] = (adapter.getItem(i)).mSongId;
            }
        }
        return list;
    }


    public static final String makeTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs -= hours * 3600;
        mins = secs / 60;
        secs -= mins * 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }
}
