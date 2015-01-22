package com.grasp.thinker.utils;

import com.grasp.thinker.IThinkerService;
import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.adapters.SongAdapter;
import com.grasp.thinker.model.Song;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by qiuzhangzhi on 15/1/11.
 */
public class MusicUtils {

    public static IThinkerService mService = null;

    private static final long[] sEmptyList = null;

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
           // mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
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
         /*   if (forceShuffle) {
                mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NORMAL);
            } else {
                mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
            }*/
   /*         final long currentId = mService.getAudioId();
            final int currentQueuePosition = getQueuePosition();
            if (position != -1 && currentQueuePosition == position && currentId == list[position]) {
                final long[] playlist = getQueue();
                if (Arrays.equals(list, playlist)) {
                    mService.play();
                    return;
                }
            }
            if (position < 0) {
                position = 0;
            }*/
            if(position == mService.getQueuePosition() && mService.isInitialized()){
                   playOrPause();
            }else{
                mService.open(list,position);
                mService.play();
            }

        } catch (final RemoteException ignored) {
        }}
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
