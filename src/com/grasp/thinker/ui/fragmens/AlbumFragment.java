package com.grasp.thinker.ui.fragmens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerConstant;
import com.grasp.thinker.adapters.AlbumAdapter;
import com.grasp.thinker.adapters.ArtistAdapter;
import com.grasp.thinker.loaders.AlbumLoader;
import com.grasp.thinker.loaders.ArtistLoader;
import com.grasp.thinker.model.Album;
import com.grasp.thinker.model.Artist;
import com.grasp.thinker.ui.activitys.AlbumActivity;

import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/13.
 */
public class AlbumFragment extends Fragment  implements LoaderManager.LoaderCallbacks<List<Album>>
        ,AdapterView.OnItemClickListener{

    public final static int SONG_LOADER = 2;

    private View mRootView;

    private ListView mListView;

    private AlbumAdapter mAlbumAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumAdapter = new AlbumAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_artistlist,container,false);

        mListView = (ListView)mRootView.findViewById(R.id.artist_list);
        mListView.setAdapter(mAlbumAdapter);
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
    }

    @Override
    public Loader<List<Album>> onCreateLoader(int i, Bundle bundle) {
        return new AlbumLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> artists) {
        mAlbumAdapter.updatData(artists);
        //   MusicUtils.refresh(mSongAdapter);

    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Album album = mAlbumAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putLong(ThinkerConstant.ID, album.mAlbumId);
        bundle.putString(ThinkerConstant.ALUBM_NAME, album.mAlbumName);

        final Intent intent = new Intent(getActivity(), AlbumActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}