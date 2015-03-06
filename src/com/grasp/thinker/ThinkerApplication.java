package com.grasp.thinker;

import com.grasp.thinker.utils.PreferenceUtils;

import android.app.Application;

/**
 * Created by qiuzhangzhi on 15/2/19.
 */
public class ThinkerApplication extends Application {

    public static int mThemeColor;

    public static int mAlarmWhich = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    public void init(){
        mThemeColor = PreferenceUtils.getInstance(this).getThemeColor(this);
    }
}
