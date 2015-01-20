package com.grasp.thinker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * Created by qiuzhangzhi on 15/1/19.
 */
public class NotificationHelper {

    private static final int APOLLO_MUSIC_SERVICE = 1;

    private MusicPlaybackService mService;

    private NotificationManager mNotificationManager;

    private Notification mNotification;

    private RemoteViews mNormalRemoteViews;

    private RemoteViews mExpandedRemoteViews;

    public NotificationHelper(final MusicPlaybackService service){
        mService = service;
        mNotificationManager = (NotificationManager)mService
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(final String albumName, final String artistName, final String trackName
            , final boolean isPlaying){

        mNormalRemoteViews = new RemoteViews(mService.getPackageName(),R.layout.notification_normal);

        mNormalRemoteViews.setTextViewText(R.id.notification_normal_line_one, trackName);
        // Artist name (line two)
        mNormalRemoteViews.setTextViewText(R.id.notification_normal_line_two, artistName);

        mNotification = new NotificationCompat.Builder(mService)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(getPendingIntent())
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        mNotification.contentView = mNormalRemoteViews;
        initPlaybackActions(isPlaying);

        mService.startForeground(APOLLO_MUSIC_SERVICE, mNotification);

    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(mService, 0, new Intent("com.grasp.thinker.main")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
    }

    private void initPlaybackActions(boolean isPlaying) {
        // Play and pause
        mNormalRemoteViews.setOnClickPendingIntent(R.id.notification_normal_play,
                retreivePlaybackActions(1));


        // Update the play button image
        mNormalRemoteViews.setImageViewResource(R.id.notification_normal_play,
                isPlaying ? R.drawable.player_pause_white : R.drawable.player_play_white);
    }

    public void updatePlayState(final boolean isPlaying) {
        if (mNotification == null || mNotificationManager == null) {
            return;
        }
        if (mNormalRemoteViews != null) {
            mNormalRemoteViews.setImageViewResource(R.id.notification_normal_play,
                    isPlaying ? R.drawable.player_pause_white : R.drawable.player_play_white);
        }

        mNotificationManager.notify(APOLLO_MUSIC_SERVICE, mNotification);
    }
    private final PendingIntent retreivePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(mService, MusicPlaybackService.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(MusicPlaybackService.TOGGLEPAUSE_ACTION);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

}
