package com.grasp.thinker.persistent;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by qiuzhangzhi on 15/3/6.
 */
public class ThinkerDatabase {

    private static final String DB_NAME = "thinker_db" ;

    private static final String TABLE_PLAYLIST = "table_playlist" ;

    private static final int DB_VERSION_1 = 1 ;

    private static final int DB_VERSION = DB_VERSION_1 ;

    private static ThinkerDatabase sInstance;

    private static  DBHelper mDBHelper;

    private static SQLiteDatabase database;

    private ThinkerDatabase(Context context){
        mDBHelper = new DBHelper(context.getApplicationContext());
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
                   "create table "+TABLE_PLAYLIST
                    +"(id integer primary key,"
                    +"name text,"
                    +"artist text,"
                    +"album text,"
                    +"duration text,"
                    +"playtime integer"
                    +");"
            );
        }
    }

}
