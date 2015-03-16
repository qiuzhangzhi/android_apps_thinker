package com.grasp.thinker.model;

import android.text.TextUtils;

/**
 * Created by qiuzhangzhi on 15/1/10.
 */
public class Song {


    public long mSongId;


    public String mSongName;


    public String mArtistName;


    public String mAlbumName;


    public int mDuration;

    public long mPlaytimes;


    public Song(final long songId, final String songName, final String artistName,
            final String albumName, final int duration, final long playTimes) {
        mSongId = songId;
        mSongName = songName;
        mArtistName = artistName;
        mAlbumName = albumName;
        mDuration = duration;
        mPlaytimes = playTimes;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mAlbumName == null ? 0 : mAlbumName.hashCode());
        result = prime * result + (mArtistName == null ? 0 : mArtistName.hashCode());
        result = prime * result + mDuration;
        result = prime * result + (int) mSongId;
        result = prime * result + (mSongName == null ? 0 : mSongName.hashCode());
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Song other = (Song)obj;
        if (mSongId != other.mSongId) {
            return false;
        }
        if (!TextUtils.equals(mAlbumName, other.mAlbumName)) {
            return false;
        }
        if (!TextUtils.equals(mArtistName, other.mArtistName)) {
            return false;
        }
        if (mDuration != other.mDuration) {
            return false;
        }
        if (!TextUtils.equals(mSongName, other.mSongName)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return mSongName;
    }
}
