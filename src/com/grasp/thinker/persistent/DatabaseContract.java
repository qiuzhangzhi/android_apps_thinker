package com.grasp.thinker.persistent;


/**
 * Created by qiuzhangzhi on 15/3/8.
 */
public class DatabaseContract {

    private DatabaseContract(){}

    public static abstract class PlayListEntry{
        public static final String TABLE_NAME = "playlist";
        public static final String COLUMN_ID ="id" ;
        public static final String COLUMN_NAME = "name" ;
        public static final String COLUMN_ARTIST = "artist" ;
        public static final String COLUMN_ALBUM = "album" ;
        public static final String COLUMN_DURATION = "duration" ;
        public static final String COLUMN_PLAYTIME = "playtime" ;
        public static final String COLUMN_DATE_ADD = "date" ;

    }

}
