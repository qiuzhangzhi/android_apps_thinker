package com.grasp.thinker.loaders;

import com.grasp.thinker.model.Song;
import com.grasp.thinker.utils.PreferenceUtils;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by qiuzhangzhi on 15/1/10.
 */
public class SongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    private final ArrayList<Song> mSongList = new ArrayList<Song>();

    private static PreferenceUtils mPreferenceUtils;

    private Cursor mCursor;
    public SongLoader(final Context context){
        super(context);
        mPreferenceUtils = PreferenceUtils.getsInstance(context);
    }

    @Override
    public List<Song> loadInBackground() {
         mCursor = getSongCursor(getContext());
        if(mCursor!=null && mCursor.moveToFirst()){
            do{

                final long id = mCursor.getLong(0);
                final String songName = mCursor.getString(1);
                final String songAlbum = mCursor.getString(2);
                final String songArtist = mCursor.getString(3);
                final long songDuration = mCursor.getLong(4);

                final int songDurationSecond = (int) songDuration/1000;

                final Song song = new Song(id,songName,songArtist,songAlbum,songDurationSecond);
                mSongList.add(song);
            }while (mCursor.moveToNext());
        }
        if(mCursor!=null){
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public final static Cursor getSongCursor(final Context context){

        Set<String> filters = mPreferenceUtils.getSongFilter();

        final StringBuilder mSelectionClause = new StringBuilder();
        mSelectionClause.append(Audio.AudioColumns.IS_MUSIC + "= 1 ");
        mSelectionClause.append("AND "+ Audio.AudioColumns.TITLE + " !='' ");

        if(mPreferenceUtils.getIsFilterNum()){
            mSelectionClause.append("AND "+ Audio.AudioColumns.TITLE + " NOT GLOB '[0-9]*' ");
        }
        if(mPreferenceUtils.getIsFilterLetter()){
            mSelectionClause.append("AND "+ Audio.AudioColumns.TITLE + " NOT GLOB '[a-z]*' ");
        }
        if(filters!=null){
            for (String filter : filters){
                mSelectionClause.append("AND "+ Audio.AudioColumns.TITLE + " NOT GLOB '" + filter +"*' ");
            }
        }


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
