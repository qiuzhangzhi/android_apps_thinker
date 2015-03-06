package com.grasp.thinker.utils;


import com.grasp.thinker.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by qiuzhangzhi on 15/2/5.
 */
public class PreferenceUtils {

    private static final String IS_FILTER_NUM ="is_filter_num";

    private static final String IS_FILTER_LETTER ="is_filter_letter";

    private static final String SONG_FILTERS ="song_filters";

    private static final String THEME_COLOR = "theme_color";

    private static final String ALARM_WHICH = "alarm_which";

    private static PreferenceUtils sInstance;

    private final SharedPreferences mPreferences;


    private PreferenceUtils(final Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtils getInstance(final Context context){
        if(sInstance == null){
            sInstance = new PreferenceUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setIsFilterNum(boolean isFilterNum){

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(IS_FILTER_NUM,isFilterNum);
        editor.apply();

    }

    public boolean getIsFilterNum(){
        return mPreferences.getBoolean(IS_FILTER_NUM,true);
    }

    public void setIsFilterLetter(boolean isFilterLetter){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(IS_FILTER_LETTER,isFilterLetter);
        editor.apply();
    }

    public boolean getIsFilterLetter(){
        return mPreferences.getBoolean(IS_FILTER_LETTER,true);
    }

    public void setSongFilters(String filters){
        Set<String> filterSet = mPreferences.getStringSet(SONG_FILTERS,null);
        if(filterSet == null){
            filterSet = new HashSet<String>();
        }
        filterSet.add(filters);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.putStringSet(SONG_FILTERS, filterSet);
        editor.apply();
    }

    public void removeSongFilter(String filter){
        Set<String> filterSet = mPreferences.getStringSet(SONG_FILTERS,null);
        if(filterSet == null){
            filterSet = new HashSet<String>();
        }

        filterSet.remove(filter);

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.putStringSet(SONG_FILTERS, filterSet);
        editor.apply();
    }
    
    public Set<String> getSongFilter(){
        return mPreferences.getStringSet(SONG_FILTERS,null);
    }

    public void setThemeColor(int color){
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(THEME_COLOR,color);
        editor.apply();
    }

    public int getThemeColor(Context context){
        return mPreferences.getInt(THEME_COLOR,context.getResources().getColor(R.color.thinker_basic_color));
    }


}
