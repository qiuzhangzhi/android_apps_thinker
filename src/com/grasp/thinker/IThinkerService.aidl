package com.grasp.thinker;

interface IThinkerService{

    void openFile(String path);
    void open(in long [] list, int position);

    void pause();
    void play();
    void next();
    void prev();

}