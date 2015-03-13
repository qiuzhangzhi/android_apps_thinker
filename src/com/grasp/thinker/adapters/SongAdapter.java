package com.grasp.thinker.adapters;

import com.grasp.thinker.R;
import com.grasp.thinker.model.Song;
import com.grasp.thinker.utils.MusicUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 15/1/10.
 */
public class SongAdapter extends BaseAdapter {

    public Context mContext;

    public ArrayList<Song> mData = new ArrayList<Song>();

    public SongAdapter(Context context){
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_songlist_adpter,null);
            viewHolder.mSongName = (TextView)convertView.findViewById(R.id.song_name);
            viewHolder.mSongInfo = (TextView)convertView.findViewById(R.id.artist_album);
            viewHolder.mSongTime = (TextView)convertView.findViewById(R.id.song_time);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Song song = getItem(position);
        if(song!=null){
            viewHolder.mSongName.setText(song.mSongName+" 播放次数："+song.mPlaytimes);
            viewHolder.mSongInfo.setText(String.format(mContext.getString(R.string.divider_artist_album),song.mArtistName,song.mAlbumName));
            viewHolder.mSongTime.setText(""+ MusicUtils.makeTimeString(mContext,song.mDuration));
        }
        return convertView;
    }

    public void updatData(List<Song> data){
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView mSongName;
        TextView mSongInfo;
        TextView mSongTime;
    }
}
