package com.grasp.thinker.ui.activitys;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerConstant;
import com.grasp.thinker.adapters.SongAdapter;
import com.grasp.thinker.loaders.AlbumSongLoader;
import com.grasp.thinker.model.Song;
import com.grasp.thinker.utils.MusicUtils;
import java.util.List;

/**
 * Created by qiuzhangzhi on 2015/3/14.
 */
public class AlbumActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Song>>,AdapterView.OnItemClickListener{

    public final static int SONG_LOADER = 5;

    private String mActionbarTitle;

    private ListView mListView;

    private SongAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListView =(ListView) findViewById(R.id.artist_song_list);
        mAdapter = new SongAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        Bundle bundle = getIntent().getExtras();
        mActionbarTitle = bundle.getString(ThinkerConstant.ALUBM_NAME);

        if(bundle != null){
            getSupportLoaderManager().initLoader(SONG_LOADER, bundle, this);
        }

        setActionbarTitle(mActionbarTitle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MusicUtils.playAllFromUserItemClick(this, mAdapter, position);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int i, Bundle bundle) {
        return new AlbumSongLoader(this,bundle.getLong(ThinkerConstant.ID));
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> songs) {
        mAdapter.updatData(songs);

    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
    }

    @Override
    public int setContentView() {
        return R.layout.activity_artist_song;
    }
}