package com.grasp.thinker.adapters;

import com.grasp.thinker.R;
import com.grasp.thinker.utils.PreferenceUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by qiuzhangzhi on 15/2/3.
 */
public class FilterAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<String> mData;

    private PreferenceUtils mPreferenceUtils;

    public FilterAdapter(Context context){
        mContext = context;
        mData = new ArrayList<String>();
        mPreferenceUtils = PreferenceUtils.getsInstance(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }


    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_filter,null);
            viewHolder = new ViewHolder();

            viewHolder.mTextView = (TextView)convertView.findViewById(R.id.filter_name);
            viewHolder.mImageView = (ImageView)convertView.findViewById(R.id.filter_remove);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        final String filterName = getItem(position);
        viewHolder.mTextView.setText(filterName);
        viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removItem(position);
                mPreferenceUtils.removeSongFilter(filterName);
            }
        });

        return convertView;
    }

    public void removItem(int position){
        mData.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(String filterName){
        mData.add(mData.size(),filterName);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<String> data){
        if(data!=null){
            mData.addAll(data);
            notifyDataSetChanged();
        }
    }
    public class ViewHolder{

        public TextView mTextView;

        public ImageView mImageView;
    }

}
