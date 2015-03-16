package com.grasp.thinker.widgets;


import com.grasp.thinker.R;
import com.grasp.thinker.utils.MusicUtils;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class RepeatingImageButton extends ImageButton implements OnClickListener {


    private static final String NEXT = "btn_playback_next";

    private static final String PREVIOUS = "btn_playback_previous";

    private static final long sInterval = 400;

    private long mStartTime;

    private int mRepeatCount;

    private RepeatListener mListener;


    public RepeatingImageButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setBackgroundResource(R.color.transparent);
        setFocusable(true);
        setLongClickable(true);
        setOnClickListener(this);
        updateState();
    }


    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.popup_button_previous:
                MusicUtils.previous();
                break;
            case R.id.popup_button_next:
                MusicUtils.next();
            default:
                break;
        }
    }


    public void setRepeatListener(final RepeatListener l) {
        mListener = l;
    }

    @Override
    public boolean performLongClick() {

        mStartTime = SystemClock.elapsedRealtime();
        mRepeatCount = 0;
        post(mRepeater);
        return true;
    }


    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            removeCallbacks(mRepeater);
            if (mStartTime != 0) {
                doRepeat(true);
                mStartTime = 0;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:

                super.onKeyDown(keyCode, event);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeCallbacks(mRepeater);
                if (mStartTime != 0) {
                    doRepeat(true);
                    mStartTime = 0;
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    private final Runnable mRepeater = new Runnable() {
        @Override
        public void run() {
            doRepeat(false);
            if (isPressed()) {
                postDelayed(this, sInterval);
            }
        }
    };

    private void doRepeat(final boolean shouldRepeat) {
        final long now = SystemClock.elapsedRealtime();
        if (mListener != null) {
            mListener.onRepeat(this, now - mStartTime, shouldRepeat ? -1 : mRepeatCount++);
        }
    }

    public void updateState() {
        switch (getId()) {
            case R.id.popup_button_next:
                setImageDrawable(getResources().getDrawable(R.drawable.player_next_base));
                break;
            case R.id.popup_button_previous:
                setImageDrawable(getResources().getDrawable(R.drawable.player_pre_base));
                break;
            default:
                break;
        }
    }

    public interface RepeatListener {
        void onRepeat(View v, long duration, int repeatcount);
    }

}
