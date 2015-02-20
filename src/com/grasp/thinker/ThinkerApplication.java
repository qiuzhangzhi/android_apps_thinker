package com.grasp.thinker;

import com.grasp.thinker.utils.PreferenceUtils;

import android.app.Application;

/**
 * Created by qiuzhangzhi on 15/2/19.
 */
public class ThinkerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initConstant();

    }

    public void initConstant(){
        ThinkerConstant.mThemeColor = PreferenceUtils.getInstance(this).getThemeColor(this);
    }
}
