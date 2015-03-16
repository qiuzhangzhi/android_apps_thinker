package com.grasp.thinker;

import com.grasp.thinker.utils.PreferenceUtils;

import android.app.Application;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }
}
