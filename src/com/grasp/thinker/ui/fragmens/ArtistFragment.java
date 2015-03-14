package com.grasp.thinker.ui.fragmens;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.AdapterView;
import android.widget.ListView;
import com.grasp.thinker.R;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.grasp.thinker.ThinkerConstant;
import com.grasp.thinker.adapters.ArtistAdapter;
import com.grasp.thinker.adapters.SongAdapter;
import com.grasp.thinker.loaders.ArtistLoader;
import com.grasp.thinker.loaders.SongLoader;
import com.grasp.thinker.model.Artist;
import com.grasp.thinker.model.Song;
import com.grasp.thinker.ui.activitys.ArtistActivity;
import com.grasp.thinker.utils.MusicUtils;

import java.util.List;

/**
 * Created by qiuzhangzhi on 15/1/6.
 */
public class ArtistFragment extends Fragment  implements LoaderManager.LoaderCallbacks<List<Artist>>
        ,AdapterView.OnItemClickListener{

    public final static int SONG_LOADER = 1;

    private View mRootView;

    private ListView mListView;

    private ArtistAdapter mArtistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistAdapter = new ArtistAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_artistlist,container,false);

        mListView = (ListView)mRootView.findViewById(R.id.artist_list);
        mListView.setAdapter(mArtistAdapter);
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
    public Loader<List<Artist>> onCreateLoader(int i, Bundle bundle) {
        return new ArtistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> artists) {
        mArtistAdapter.updatData(artists);
     //   MusicUtils.refresh(mSongAdapter);

    }

    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Artist mArtist = mArtistAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), ArtistActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(ThinkerConstant.ID, mArtist.mArtistId);
        bundle.putString(ThinkerConstant.ARTIST_NAME,mArtist.mArtistName);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}