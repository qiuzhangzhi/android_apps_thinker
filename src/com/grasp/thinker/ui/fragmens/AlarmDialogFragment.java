package com.grasp.thinker.ui.fragmens;

import com.grasp.thinker.MusicPlaybackService;
import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerApplication;
import com.grasp.thinker.utils.PreferenceUtils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.Arrays;

/**
 * Created by qiuzhangzhi on 15/3/3.
 */
public class AlarmDialogFragment extends DialogFragment {

    private static final String TAG = "AlarmDialogFragment";

    private boolean DEBUG = true;

    private View mTitleView;

    private View mTitleDivider;

    private static final int[] minutes = {0,10,20,30,50,70,90};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitleView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_alarm_title,null);
        mTitleDivider = mTitleView.findViewById(R.id.alarm_divider);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.dialog_alarm_item, android.R.id.text1, getResources().getStringArray(R.array.alarm_times));
        mTitleDivider.setBackgroundColor(ThinkerApplication.mThemeColor);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_LIGHT);
                builder.setCustomTitle(mTitleView)
               .setSingleChoiceItems(adapter, ThinkerApplication.mAlarmWhich, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                       Intent intent = new Intent(getActivity(),MusicPlaybackService.class);
                       intent.setAction(MusicPlaybackService.EXIT);
                       PendingIntent pendingIntent = PendingIntent.getService(getActivity(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                       AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                       if(which != 0){
                           alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()+minutes[which]*60*1000, pendingIntent);
                       }else{
                           alarmManager.cancel(pendingIntent);
                       }

                       ThinkerApplication.mAlarmWhich = which ;
                       dismiss();

                   }
               });

        final AlertDialog dialog = builder.show();
        final Resources res = getResources();
        final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = dialog.findViewById(titleDividerId);
        if(titleDivider != null){
            titleDivider.setVisibility(View.GONE);
        }

        return dialog;
    }
}