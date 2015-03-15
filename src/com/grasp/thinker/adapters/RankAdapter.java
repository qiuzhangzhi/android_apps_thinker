package com.grasp.thinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.grasp.thinker.R;
import com.grasp.thinker.model.Song;
import com.grasp.thinker.widgets.TextProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/15.
 */
public class RankAdapter extends BaseAdapter {

    private static final int INIT_PROGRESS_MAX = 50;

    private int max  = INIT_PROGRESS_MAX;

    public Context mContext;

    public ArrayList<Song> mData = new ArrayList<Song>();

    public RankAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Song getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).mSongId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_ranklist_adapter,null);
            viewHolder.mSongInfo = (TextView)convertView.findViewById(R.id.artist_album);
            viewHolder.mSongProgress = (TextProgressBar)convertView.findViewById(R.id.rank_progress);
            viewHolder.mProgressText = (TextView)convertView.findViewById(R.id.progress_text);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Song song = getItem(position);
        if(song!=null){
            if(position == 0 && song.mPlaytimes >= INIT_PROGRESS_MAX){
                max = 100 * (int)((song.mPlaytimes)/100 + 1);
            }
            viewHolder.mSongProgress.setMax(max);
            viewHolder.mSongProgress.setIndicator(viewHolder.mProgressText);
            viewHolder.mProgressText.setText("" + song.mPlaytimes);
            viewHolder.mSongProgress.setProgress((int)song.mPlaytimes);
            viewHolder.mSongInfo.setText(song.mSongName);

        }
        return convertView;
    }

    public void updatData(List<Song> data){
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView mSongInfo;
        TextProgressBar mSongProgress;
        TextView mProgressText;
    }
}
