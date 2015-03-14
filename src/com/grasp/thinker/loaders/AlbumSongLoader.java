package com.grasp.thinker.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import com.grasp.thinker.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/14.
 */

public class AlbumSongLoader extends WrappedAsyncTaskLoader<List<Song>> {


    private final ArrayList<Song> mSongList = new ArrayList<Song>();

    private Cursor mCursor;

    private final Long mAlbumID;

    public AlbumSongLoader(final Context context, final Long albumId) {
        super(context);
        mAlbumID = albumId;
    }

    @Override
    public List<Song> loadInBackground() {
        mCursor = makeAlbumSongCursor(getContext(), mAlbumID);
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);

                final String songName = mCursor.getString(1);

                final String artist = mCursor.getString(2);

                final String album = mCursor.getString(3);

                final long duration = mCursor.getLong(4);

                final int seconds = (int) (duration / 1000);

                final Song song = new Song(id, songName, artist, album, seconds,0);

                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public static final Cursor makeAlbumSongCursor(final Context context, final Long albumId) {
        final StringBuilder selection = new StringBuilder();
        selection.append(AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + AudioColumns.TITLE + " != ''");
        selection.append(" AND " + AudioColumns.ALBUM_ID + "=" + albumId);
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        AudioColumns.TITLE,
                        AudioColumns.ARTIST,
                        AudioColumns.ALBUM,
                        AudioColumns.DURATION
                }, selection.toString(), null,
                null);
    }

}
