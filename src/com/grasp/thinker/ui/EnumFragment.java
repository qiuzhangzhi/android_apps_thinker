package com.grasp.thinker.ui;

import com.grasp.thinker.ui.fragmens.AlbumFragment;
import com.grasp.thinker.ui.fragmens.PlaylistFragment;
import com.grasp.thinker.ui.fragmens.ArtistFragment;

import android.support.v4.app.Fragment;
import com.grasp.thinker.ui.fragmens.RankFragment;

/**
 * Created by qiuzhangzhi on 15/1/7.
 */
public enum EnumFragment {

    PLAYLIST(PlaylistFragment.class),

    ARTIST(ArtistFragment.class),

    ALBUM(AlbumFragment.class),

    RANK(RankFragment.class);

    private Class<? extends Fragment> mFragmentClass;

    private EnumFragment(final Class<? extends Fragment> fragmentClass) {
        mFragmentClass = fragmentClass;
    }

    public Class<? extends Fragment> getFragmentClass(){
        return mFragmentClass;
    }
}
