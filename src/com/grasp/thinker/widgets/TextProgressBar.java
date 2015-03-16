package com.grasp.thinker.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by qiuzhangzhi on 2015/3/15.
 */
public class TextProgressBar extends ProgressBar {

    private TextView mText;

    public TextProgressBar(Context context) {
        super(context);
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIndicator(TextView text) {
            mText = text;
    }

    @Override
    public synchronized void setProgress(final int progress) {
        if (mText != null) {
           this.post(new Runnable() {
               @Override
               public void run() {
                   float r = (5 * ((getMax() / (float)getWidth()))) ;

                   float textPosition = ((getWidth() / (float)getMax()) * (progress+r)) + getLeft();
                   int position = (int) Math.ceil(textPosition);

                   setTextPositon(mText, position);
               }
           });
        }
        super.setProgress(progress);
    }

    private void setTextPositon(TextView v, int position) {
        if (v != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v
                    .getLayoutParams();
            layoutParams.leftMargin = position;
            v.setLayoutParams(layoutParams);
        }
    }


}
