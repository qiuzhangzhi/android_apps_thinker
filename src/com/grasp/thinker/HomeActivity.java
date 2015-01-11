package com.grasp.thinker;

import com.grasp.thinker.adapters.PageAdapter;
import com.grasp.thinker.ui.fragment.EnumFragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by qiuzhangzhi on 15/1/4.
 */
public class HomeActivity extends FragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener{

    private final static int LOCAL_MUSIC = 0;

    private final static int SETTING = 1;

    private ActionBar mActionBar;

    private View mCustomActionBarView;

    private ViewPager mViewPager;

    private PageAdapter mPageAdapter;

    private TextView mActionBarLocalMusic;

    private TextView mActionBarSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
        findViews();
        setListener();

    }

    private void init(){

        mCustomActionBarView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.actionbar_custom,null);

        mViewPager = (ViewPager)findViewById(R.id.view_pager);
        mPageAdapter = new PageAdapter(this);
        final EnumFragment[] mFragments = EnumFragment.values();
        for(final EnumFragment fragment : mFragments){
            mPageAdapter.add(fragment.getFragmentClass(),null);
        }

        mViewPager.setAdapter(mPageAdapter);

        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowHomeEnabled (false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        mActionBar.setCustomView(mCustomActionBarView);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        switch (i){
            case LOCAL_MUSIC:
                mActionBarLocalMusic.setBackgroundResource(R.color.actionbar_pressed_background);
                mActionBarSetting.setBackgroundResource(R.color.transparent);
                break;
            case SETTING:
                mActionBarSetting.setBackgroundResource(R.color.actionbar_pressed_background);
                mActionBarLocalMusic.setBackgroundResource(R.color.transparent);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_local:
                mViewPager.setCurrentItem(LOCAL_MUSIC);
                break;
            case R.id.action_setting:
                mViewPager.setCurrentItem(SETTING);
                break;
        }
    }

    private void findViews(){
        mActionBarLocalMusic = (TextView)mCustomActionBarView.findViewById(R.id.action_local);
        mActionBarSetting = (TextView)mCustomActionBarView.findViewById(R.id.action_setting);
    }

    private void setListener(){
        mViewPager.setOnPageChangeListener(this);

        mActionBarLocalMusic.setOnClickListener(this);
        mActionBarSetting.setOnClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        return super.onCreateOptionsMenu(menu);
    }


}