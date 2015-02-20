/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

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

/**
 * A custom {@link android.widget.ImageButton} that represents the "repeat" button.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class RepeatButton extends ImageButton implements OnClickListener, OnLongClickListener {

    /**
     * Repeat one theme resource
     */
    private static final String REPEAT_ALL = "player_repeat_all";

    /**
     * Repeat one theme resource
     */
    private static final String REPEAT_CURRENT = "player_repeat_current";



    /**
     * The resources to use.
     */
    private final ThemeUtils mResources;

    /**
     * @param context The {@link android.content.Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    @SuppressWarnings("deprecation")
    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // Initialze the theme resources
        mResources = new ThemeUtils(context);
        // Set the selector
       // setBackgroundDrawable(new HoloSelector(context));
        // Control playback (cycle repeat modes)
        setOnClickListener(this);
        // Show the cheat sheet
        setOnLongClickListener(this);

        setBackgroundColor(getResources().getColor(R.color.transparent));

        updateRepeatState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onLongClick(final View view) {
        if (TextUtils.isEmpty(view.getContentDescription())) {
            return false;
        } else {
            ThinkerUtils.showCheatSheet(view);
            return true;
        }
    }

    /**
     * Sets the correct drawable for the repeat state.
     */
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
