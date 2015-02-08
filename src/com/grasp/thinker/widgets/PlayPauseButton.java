package com.grasp.thinker.widgets;
import com.grasp.thinker.R;
import com.grasp.thinker.utils.MusicUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by qiuzhangzhi on 15/1/15.
 */
public class PlayPauseButton extends ImageButton implements View.OnClickListener {

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.color.transparent);
        setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        MusicUtils.playOrPause();
        updateState();

    }
    public void updateState(){
        if(MusicUtils.isPlaying()){
            setImageResource(R.drawable.player_pause);
        }else{
            setImageResource(R.drawable.player_play);
        }
    }
}