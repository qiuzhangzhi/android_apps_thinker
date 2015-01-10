package com.grasp.thinker.ui.fragment;

import com.grasp.thinker.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by qiuzhangzhi on 15/1/5.
 */
public class PlaylistFragment extends Fragment {

    private View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        mRootView = inflater.inflate(R.layout.fragment_playlist,container,false);

        return mRootView;
    }
}