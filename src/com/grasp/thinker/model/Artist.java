package com.grasp.thinker.model;

import android.text.TextUtils;

/**
 * Created by qiuzhangzhi on 2015/3/13.
 */
public class Artist {


    public long mArtistId;


    public String mArtistName;


    public int mSongNumber;


    public Artist(final long artistId, final String artistName, final int songNumber) {
        mArtistId = artistId;
        mArtistName = artistName;
        mSongNumber = songNumber;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) mArtistId;
        result = prime * result + (mArtistName == null ? 0 : mArtistName.hashCode());
        result = prime * result + mSongNumber;
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
        final Artist other = (Artist)obj;

        if (mArtistId != other.mArtistId) {
            return false;
        }
        if (!TextUtils.equals(mArtistName, other.mArtistName)) {
            return false;
        }
        if (mSongNumber != other.mSongNumber) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return mArtistName;
    }

}
