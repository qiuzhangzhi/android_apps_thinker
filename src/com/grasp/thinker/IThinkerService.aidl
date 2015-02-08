package com.grasp.thinker;

interface IThinkerService{

    void openFile(String path);
    void open(in long [] list, int position);

    void pause();
    void play();
    void next();
    void prev();
    long position();
    long duration();
    void seek(long pos);
    boolean isPlaying();
    boolean isInitialized();
    int getQueuePosition();
    void refresh(in long[] list);

    String getArtistName();
    String getTrackName();
    String getAlbumName();

}