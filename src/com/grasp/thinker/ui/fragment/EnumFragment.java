package com.grasp.thinker.ui.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by qiuzhangzhi on 15/1/7.
 */
public enum EnumFragment {

    PLAYLIST(PlaylistFragment.class),

    SETTING(SettingFragment.class);

    private Class<? extends Fragment> mFragmentClass;

    private EnumFragment(final Class<? extends Fragment> fragmentClass) {
        mFragmentClass = fragmentClass;
    }

    public Class<? extends Fragment> getFragmentClass(){
        return mFragmentClass;
    }
}