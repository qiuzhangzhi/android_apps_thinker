package com.grasp.thinker.widgets;

import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.utils.MusicUtils;
import com.grasp.thinker.utils.ThemeUtils;
import com.grasp.thinker.utils.ThinkerUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

public class RepeatButton extends ImageButton implements OnClickListener, OnLongClickListener {


    private static final String REPEAT_ALL = "player_repeat_all";

    private static final String REPEAT_CURRENT = "player_repeat_current";

    private final ThemeUtils mResources;

    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mResources = new ThemeUtils(context);
        setOnClickListener(this);
        setOnLongClickListener(this);

        setBackgroundColor(getResources().getColor(R.color.transparent));

        updateRepeatState();
    }


    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }


    @Override
    public boolean onLongClick(final View view) {
        if (TextUtils.isEmpty(view.getContentDescription())) {
            return false;
        } else {
            ThinkerUtils.showCheatSheet(view);
            return true;
        }
    }

    public void updateRepeatState() {
        switch (MusicUtils.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                setContentDescription(getResources().getString(R.string.discription_repeat_all));
                setImageDrawable(mResources.getDrawable(REPEAT_ALL));
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                setContentDescription(getResources().getString(R.string.discription_repeat_current));
                setImageDrawable(mResources.getDrawable(REPEAT_CURRENT));
                break;
            default:
                setImageDrawable(mResources.getDrawable(REPEAT_ALL));
                break;
        }
    }
}
