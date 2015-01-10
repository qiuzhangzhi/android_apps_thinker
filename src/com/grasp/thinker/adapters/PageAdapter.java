package com.grasp.thinker.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 15/1/5.
 */
public class PageAdapter extends FragmentPagerAdapter {

    private List<Holder> mHolderList = new ArrayList<Holder>();

    private FragmentActivity mFragmentActivity;

    public PageAdapter(final FragmentActivity fragmentActivity) {
        super(fragmentActivity.getSupportFragmentManager());
        mFragmentActivity = fragmentActivity;

    }

    public void add(final Class<? extends Fragment> className, final Bundle params ){
        final Holder holder = new Holder();
        holder.mClassName = className.getName();
        holder.mParams = params;

        final int mPosition = mHolderList.size();
        mHolderList.add(mPosition,holder);
        notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int i) {
        final Holder holder = mHolderList.get(i);
        return Fragment.instantiate(mFragmentActivity,holder.mClassName,holder.mParams);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return mHolderList.size();
    }

    private final static class Holder{

        String mClassName;

        Bundle mParams;
    }

}
