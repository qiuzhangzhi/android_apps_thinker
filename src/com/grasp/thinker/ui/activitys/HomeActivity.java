package com.grasp.thinker.ui.activitys;

import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerApplication;
import com.grasp.thinker.adapters.PageAdapter;
import com.grasp.thinker.ui.EnumFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

/**
 * Created by qiuzhangzhi on 15/1/4.
 */
public class HomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener
{

    private final static boolean DEBUG = true;

    private final static String TAG = "HomeActivity";

    private final static int PLAYLIST = 0;

    private final static int ARTIST = 1;

    private final static int ALBUM = 2;

    private final static int RANK = 3;

    private ViewPager mViewPager;

    private PageAdapter mPageAdapter;

    // actionbar tab
    private View mIndicatorPlaylist;

    private View mIndicatorArtist;

    private View mIndicatorAlbum;

    private View mIndicatorRank;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        init();
        setListener();

    }

    private void init(){
        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mPageAdapter = new PageAdapter(this);
        final EnumFragment[] mFragments = EnumFragment.values();
        for(final EnumFragment fragment : mFragments){
            mPageAdapter.add(fragment.getFragmentClass(),null);
        }

        mViewPager.setAdapter(mPageAdapter);

        mIndicatorPlaylist.setBackgroundColor(ThinkerApplication.mThemeColor);
        mIndicatorArtist.setBackgroundColor(ThinkerApplication.mThemeColor);
        mIndicatorAlbum.setBackgroundColor(ThinkerApplication.mThemeColor);
        mIndicatorRank.setBackgroundColor(ThinkerApplication.mThemeColor);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        switch (i){
            case PLAYLIST:
                mIndicatorPlaylist.setVisibility(View.VISIBLE);
                mIndicatorArtist.setVisibility(View.INVISIBLE);
                mIndicatorAlbum.setVisibility(View.INVISIBLE);
                mIndicatorRank.setVisibility(View.INVISIBLE);
                break;
            case ARTIST:
                mIndicatorPlaylist.setVisibility(View.INVISIBLE);
                mIndicatorArtist.setVisibility(View.VISIBLE);
                mIndicatorAlbum.setVisibility(View.INVISIBLE);
                mIndicatorRank.setVisibility(View.INVISIBLE);
                break;
            case ALBUM:
                mIndicatorPlaylist.setVisibility(View.INVISIBLE);
                mIndicatorArtist.setVisibility(View.INVISIBLE);
                mIndicatorAlbum.setVisibility(View.VISIBLE);
                mIndicatorRank.setVisibility(View.INVISIBLE);
                break;
            case RANK:
                mIndicatorPlaylist.setVisibility(View.INVISIBLE);
                mIndicatorArtist.setVisibility(View.INVISIBLE);
                mIndicatorAlbum.setVisibility(View.INVISIBLE);
                mIndicatorRank.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.playlist_layout:
                mViewPager.setCurrentItem(PLAYLIST);
                break;
            case R.id.artist_layout:
                mViewPager.setCurrentItem(ARTIST);
                break;
            case R.id.album_layout:
                mViewPager.setCurrentItem(ALBUM);
                break;
            case R.id.rank_layout:
                mViewPager.setCurrentItem(RANK);
                break;
        }
    }

    private void findViews(){
        mIndicatorPlaylist = findViewById(R.id.indicator_playlist);
        mIndicatorArtist = findViewById(R.id.indicator_artist);
        mIndicatorAlbum = findViewById(R.id.indicator_album);
        mIndicatorRank = findViewById(R.id.indicator_rank);
    }

    private void setListener() {
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void updateColor(int color) {
        super.updateColor(color);
        mIndicatorPlaylist.setBackgroundColor(color);
        mIndicatorArtist.setBackgroundColor(color);
        mIndicatorAlbum.setBackgroundColor(color);
        mIndicatorRank.setBackgroundColor(color);
    }

    @Override
    public int setContentView() {
        return R.layout.activity_home;
    }
}

