package com.grasp.thinker.loaders;

import com.grasp.thinker.model.Song;
import com.grasp.thinker.persistent.ThinkerDatabase;
import com.grasp.thinker.utils.PreferenceUtils;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by qiuzhangzhi on 15/1/10.
 */
public class SongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private static final String TAG = "SongLoader" ;

    private static final boolean DEBUG = true;

    private int i = 0;
    private final ArrayList<Song> mSongList = new ArrayList<Song>();

    private Cursor mCursor;

    public SongLoader(final Context context){
        super(context);
    }

    @Override
    public List<Song> loadInBackground() {

        mSongList.addAll(ThinkerDatabase.getsInstance(getContext()).getPlaylist());

        if(mSongList == null || mSongList.size() == 0){
            if (DEBUG) {
                Log.d("qunimabi"," "+i++);
            }
            mCursor = getSongCursor(getContext());
            ThinkerDatabase.getsInstance(getContext()).initDatabase(mCursor);
            mSongList.addAll(ThinkerDatabase.getsInstance(getContext()).getPlaylist());
            if (mCursor != null){
                mCursor.close();
                mCursor =null;
            }
        }
        return mSongList;
    }

    public final static Cursor getSongCursor(final Context context){


        final StringBuilder mSelectionClause = new StringBuilder();
        mSelectionClause.append(Audio.AudioColumns.IS_MUSIC + "= 1 ");
        mSelectionClause.append("AND "+ Audio.AudioColumns.TITLE + " !='' ");
        return context.getContentResolver().query(

                Audio.Media.EXTERNAL_CONTENT_URI,

                new String[]{
                        BaseColumns._ID,
                        Audio.AudioColumns.TITLE,
                        Audio.AudioColumns.ALBUM,
                        Audio.AudioColumns.ARTIST,
                        Audio.AudioColumns.DURATION,

                },

                mSelectionClause.toString(),

                null,

                MediaStore.Audio.Media.DATE_ADDED + " DESC"
        );
    }
}
