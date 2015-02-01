package com.grasp.thinker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

/**
 * Created by qiuzhangzhi on 15/1/28.
 */
public class ThemeUtils {

    private static final String PACKAGE_NAME = "theme_package_name";

    private static final String THINKER_PACKAGE = "com.grasp.thinker";

    private static String mThemePackage;

    private final PackageManager mPackageManager;

    private final SharedPreferences mPreferences;

    private Resources mResources;

    public ThemeUtils(Context context){

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Get the theme package name
        mThemePackage = getThemePackageName();

        // Initialze the package manager
        mPackageManager = context.getPackageManager();
        try {
            // Find the theme resources
            mResources = mPackageManager.getResourcesForApplication(mThemePackage);
        } catch (final Exception e) {
            // If the user isn't using a theme, then the resources should be
            // Apollo's.
            setThemePackageName(THINKER_PACKAGE);
        }
    }

    public final String getThemePackageName() {
        return mPreferences.getString(PACKAGE_NAME, THINKER_PACKAGE);
    }

    public final void setThemePackageName(final String packageName){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PACKAGE_NAME,packageName);
        editor.apply();
    }

    public Drawable getDrawable(final String resourceName) {
        final int resourceId = mResources.getIdentifier(resourceName, "drawable", mThemePackage);
        try {
            return mResources.getDrawable(resourceId);
        } catch (final Resources.NotFoundException e) {
            //$FALL-THROUGH$
        }
        return null;
    }

    public int getColor(final String resourceName) {
        final int resourceId = mResources.getIdentifier(resourceName, "color", mThemePackage);
        try {
            return mResources.getColor(resourceId);
        } catch (final Resources.NotFoundException e) {
            // If the theme designer wants to allow the user to theme a
            // particular object via the color picker, they just remove the
            // resource item from the themeconfig.xml file.
        }
        return 0;
    }
}
