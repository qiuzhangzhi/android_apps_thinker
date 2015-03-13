package com.grasp.thinker.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.grasp.thinker.R;
import com.grasp.thinker.model.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/13.
 */
public class AlbumAdapter extends BaseAdapter{

    public Context mContext;

    public ArrayList<Album> mData = new ArrayList<Album>();

    public AlbumAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Album getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).mAlbumId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_albumlist_adapter,null);
            viewHolder.mAlbumIco = (ImageView)convertView.findViewById(R.id.album_ico);
            viewHolder.mAlbumName = (TextView)convertView.findViewById(R.id.album_name);
            viewHolder.mAlbumInfo = (TextView)convertView.findViewById(R.id.album_info);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Album album = getItem(position);
        if(album!=null){
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.ic_launcher)
                    .build();

            imageLoader.displayImage("content://media/external/audio/albumart/"+album.mAlbumId, viewHolder.mAlbumIco,options);
            viewHolder.mAlbumName.setText(album.mAlbumName);
            viewHolder.mAlbumInfo.setText(String.format(mContext.getString(R.string.album_item_info), album.mSongNumber, album.mArtistName));
        }
        return convertView;
    }

    public void updatData(List<Album> data){
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        ImageView mAlbumIco;
        TextView mAlbumName;
        TextView mAlbumInfo;
    }
}
