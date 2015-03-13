package com.grasp.thinker.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import com.grasp.thinker.model.Artist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/13.
 */
public class ArtistLoader extends  WrappedAsyncTaskLoader<List<Artist>> {


    private final ArrayList<Artist> mArtistsList = new ArrayList<Artist>();


    private Cursor mCursor;


    public ArtistLoader(final Context context) {
        super(context);
    }


    @Override
    public List<Artist> loadInBackground() {
        mCursor = makeArtistCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);

                final String artistName = mCursor.getString(1);

                final int songCount = mCursor.getInt(2);


                final Artist artist = new Artist(id, artistName, songCount);

                mArtistsList.add(artist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mArtistsList;
    }


    public static final Cursor makeArtistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                }, null, null,null);
    }
}
