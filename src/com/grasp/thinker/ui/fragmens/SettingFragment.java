package com.grasp.thinker.ui.fragmens;

import com.grasp.thinker.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by qiuzhangzhi on 15/1/6.
 */
public class SettingFragment extends Fragment {

    private View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_setting,container,false);

        return mRootView;
    }
}