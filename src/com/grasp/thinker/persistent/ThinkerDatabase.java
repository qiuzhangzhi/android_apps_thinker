package com.grasp.thinker.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.grasp.thinker.model.Song;
import com.grasp.thinker.persistent.DatabaseContract.*;
import com.grasp.thinker.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by qiuzhangzhi on 15/3/6.
 */
public class ThinkerDatabase {

    private static final String DB_NAME = "thinker_db" ;

    private static final int DB_VERSION_1 = 1 ;

    private static final int DB_VERSION = DB_VERSION_1 ;

    private static ThinkerDatabase sInstance;

    private static  DBHelper mDBHelper;

    private static SQLiteDatabase database;

    private static PreferenceUtils mPreferenceUtils;

    private ThinkerDatabase(Context context){
        mDBHelper = new DBHelper(context.getApplicationContext());
        open();
        mPreferenceUtils = PreferenceUtils.getInstance(context);
    }

    public static ThinkerDatabase getsInstance(Context context){
        if(sInstance == null){
            sInstance = new ThinkerDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    private void open() throws SQLException{
        database = mDBHelper.getWritableDatabase();
    }


    public void initDatabase(Cursor cursor){

        if(cursor != null && cursor.moveToLast()){
            do{
                ContentValues values = new ContentValues();
                values.put(PlayListEntry.COLUMN_ID,cursor.getLong(0));
                values.put(PlayListEntry.COLUMN_NAME, cursor.getString(1));
                values.put(PlayListEntry.COLUMN_ALBUM,cursor.getString(2));
                values.put(PlayListEntry.COLUMN_ARTIST,cursor.getString(3));
                values.put(PlayListEntry.COLUMN_DURATION, cursor.getLong(4));
                database.insert(PlayListEntry.TABLE_NAME,null,values);
            }while (cursor.moveToPrevious());
        }

        if(cursor != null){
            cursor.close();
            cursor = null;
        }

    }

    public ArrayList<Song> getPlaylist(){
        ArrayList<Song> result = new ArrayList<Song>();

        Set<String> filters = mPreferenceUtils.getSongFilter();

        final StringBuilder mSelectionClause = new StringBuilder();
        if(mPreferenceUtils.getIsFilterNum()){
            mSelectionClause.append( PlayListEntry.COLUMN_NAME + " NOT GLOB '[0-9]*' ");
        }
        if(mPreferenceUtils.getIsFilterLetter()){
            mSelectionClause.append("AND "+ PlayListEntry.COLUMN_NAME + " NOT GLOB '[a-z]*' ");
        }
        if(filters!=null){
            for (String filter : filters){
                mSelectionClause.append("AND "+ PlayListEntry.COLUMN_NAME + " NOT GLOB '" + filter +"*' ");
            }
        }
        String[] projection ={PlayListEntry.COLUMN_ID,PlayListEntry.COLUMN_NAME,PlayListEntry.COLUMN_ALBUM,PlayListEntry.COLUMN_ARTIST,PlayListEntry.COLUMN_DURATION};
        Cursor cursor = database.query(PlayListEntry.TABLE_NAME, projection,mSelectionClause.toString(),null,null,null,null);
        if(cursor != null && cursor.moveToFirst()) {
            do {
                final long id = cursor.getLong(0);
                final String songName = cursor.getString(1);
                final String songAlbum = cursor.getString(2);
                final String songArtist = cursor.getString(3);
                final long songDuration = cursor.getLong(4);

                final int songDurationSecond = (int) songDuration/1000;

                final Song song = new Song(id,songName,songArtist,songAlbum,songDurationSecond);

                result.add(song);
            }while (cursor.moveToNext());

        }
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
        return result;
    }

    private class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context){
            super(context,DB_NAME,null,DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTablePlaylist(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private void createTablePlaylist( SQLiteDatabase db){

            db.execSQL(
                   "create table "+ PlayListEntry.TABLE_NAME
                    +"("+PlayListEntry.COLUMN_ID+" integer primary key,"
                    +PlayListEntry.COLUMN_NAME+" text,"
                    +PlayListEntry.COLUMN_ARTIST+" text,"
                    +PlayListEntry.COLUMN_ALBUM+" text,"
                    +PlayListEntry.COLUMN_DURATION+" text,"
                    +PlayListEntry.COLUMN_PLAYTIME+" integer"
                    +");"
            );
        }
    }

}
