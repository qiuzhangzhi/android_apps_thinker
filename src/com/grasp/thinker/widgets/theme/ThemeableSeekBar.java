package com.grasp.thinker.widgets.theme;


import com.grasp.thinker.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;


public class ThemeableSeekBar extends SeekBar {


    public static final String PROGESS = "audio_player_seekbar";


    public ThemeableSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setProgressDrawable(getResources().getDrawable(R.drawable.lay_seekbar));
    }

}
