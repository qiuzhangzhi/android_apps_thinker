package com.grasp.thinker.ui.fragmens;

import com.grasp.thinker.R;
import com.grasp.thinker.adapters.SongAdapter;
import com.grasp.thinker.loaders.SongLoader;
import com.grasp.thinker.model.Song;
import com.grasp.thinker.utils.MusicUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by qiuzhangzhi on 15/1/5.
 */
public class PlaylistFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>>
,AdapterView.OnItemClickListener{

    public final static int SONG_LOADER = 0;

    private View mRootView;

    private ListView mListView;

    private SongAdapter mSongAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongAdapter = new SongAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        mRootView = inflater.inflate(R.layout.fragment_playlist,container,false);

        mListView = (ListView)mRootView.findViewById(R.id.song_list);
        mListView.setAdapter(mSongAdapter);
        mListView.setOnItemClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SONG_LOADER,null,this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSongAdapter.getCount() != 0){
            getLoaderManager().restartLoader(SONG_LOADER, null, this);
        }
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int i, Bundle bundle) {
        return new SongLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> songs) {
            mSongAdapter.updatData(songs);
            MusicUtils.refresh(mSongAdapter);

    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        MusicUtils.playAllFromUserItemClick(getActivity(),mSongAdapter,position);
    }
}