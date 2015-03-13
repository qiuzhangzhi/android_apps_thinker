package com.grasp.thinker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.grasp.thinker.R;
import com.grasp.thinker.model.Artist;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/13.
 */
public class ArtistAdapter extends BaseAdapter {

    public Context mContext;

    public ArrayList<Artist> mData = new ArrayList<Artist>();

    public ArtistAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Artist getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).mArtistId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_artistlist_adapter,null);
            viewHolder.mArtistIco = (ImageView)convertView.findViewById(R.id.artist_ico);
            viewHolder.mArtistName = (TextView)convertView.findViewById(R.id.artist_name);
            viewHolder.mSongNum = (TextView)convertView.findViewById(R.id.song_num);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Artist artist = getItem(position);
        if(artist!=null){
            /*ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage("content://media/external/audio/albumart/"+artist.mArtistId, viewHolder.mArtistIco);*/
            viewHolder.mArtistName.setText(artist.mArtistName);
            viewHolder.mSongNum.setText(String.format(mContext.getString(R.string.song_num),artist.mSongNumber));
        }
        return convertView;
    }

    public void updatData(List<Artist> data){
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        ImageView mArtistIco;
        TextView mArtistName;
        TextView mSongNum;
    }
}
