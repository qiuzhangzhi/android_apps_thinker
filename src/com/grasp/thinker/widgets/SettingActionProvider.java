package com.grasp.thinker.widgets;

import com.grasp.thinker.R;

import android.app.ActionBar;
import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by qiuzhangzhi on 15/2/1.
 */
public class SettingActionProvider extends ActionProvider {

    private Context mContext;

    public SettingActionProvider(Context context){
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {

      /*  LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.actionbar_provider_setting, null);
        view.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        ImageButton button = (ImageButton) view.findViewById(R.id.action_setting);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));*/
        return null;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        subMenu.clear();
        super.onPrepareSubMenu(subMenu);
        subMenu.add(mContext.getString(R.string.actionbar_setting_filter)).setIcon(R.drawable.menu_filter_ic)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return true;
                    }
                });
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }
}
