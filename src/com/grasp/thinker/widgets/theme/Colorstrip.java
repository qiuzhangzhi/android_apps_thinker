package com.grasp.thinker.widgets.theme;


import com.grasp.thinker.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class Colorstrip extends View {


    private static final String COLORSTRIP = "colorstrip";


    public Colorstrip(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.shp_actionbar_bottom_strip);
    }
}
