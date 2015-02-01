package com.grasp.thinker.ui.fragmens;

import com.grasp.thinker.R;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

/**
 * Created by qiuzhangzhi on 15/1/6.
 */
public class SettingFragment extends Fragment {
    private static final int OPEN_IN_PLAY_STORE = 0;

    private GridView mGridView;

    private PackageManager mPackageManager;

    private List<ResolveInfo> mThemes;

    private String[] mEntries;

    private String[] mValues;

    private Drawable[] mThemePreview;

    private Resources mThemeResources;

    private String mThemePackageName;

    private String mThemeName;


    private View mRootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_setting,container,false);
        final Intent apolloThemeIntent = new Intent("com.andrew.apollo.THEMES");
        apolloThemeIntent.addCategory("android.intent.category.DEFAULT");

        mPackageManager = getActivity().getPackageManager();
        mThemes = mPackageManager.queryIntentActivities(apolloThemeIntent,PackageManager.GET_RESOLVED_FILTER);
        mEntries = new String[mThemes.size() + 1];
        mValues = new String[mThemes.size() + 1];
        mThemePreview = new Drawable[mThemes.size() + 1];

        // Default items
        mEntries[0] = getString(R.string.app_name);
        // mValues[0] = ThemeUtils.APOLLO_PACKAGE;

        for (int i = 0; i < mThemes.size(); i++) {
            mThemePackageName = mThemes.get(i).activityInfo.packageName.toString();
            mThemeName = mThemes.get(i).loadLabel(mPackageManager).toString();
            mEntries[i + 1] = mThemeName;
            mValues[i + 1] = mThemePackageName;

            // Theme resources
            try {
                mThemeResources = mPackageManager.getResourcesForApplication(mThemePackageName
                        .toString());
            } catch (final PackageManager.NameNotFoundException ignored) {
            }

            // Theme preview
            final int previewId = mThemeResources.getIdentifier("theme_preview", "drawable", //$NON-NLS-2$
                    mThemePackageName.toString());
            if (previewId != 0) {
                mThemePreview[i + 1] = mThemeResources.getDrawable(previewId);
            }
        }


        return mRootView;
    }
}