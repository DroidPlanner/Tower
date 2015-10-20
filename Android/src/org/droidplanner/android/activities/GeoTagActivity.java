package org.droidplanner.android.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.actionbar.ActionBarGeoTagFragment;
import org.droidplanner.android.fragments.geotag.GeoTagImagesFragment;
import org.droidplanner.android.fragments.geotag.GetCameraLogsFragment;

/**
 * Created by chavi on 10/15/15.
 */
public class GeoTagActivity extends DrawerNavigationUI {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geotag);



        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (fragment == null) {
            fragment = new GetCameraLogsFragment();
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_geotag;
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarGeoTag = fm.findFragmentById(toolbarId);
        if (actionBarGeoTag == null) {
            actionBarGeoTag = new ActionBarGeoTagFragment();
            fm.beginTransaction().add(toolbarId, actionBarGeoTag).commit();
        }
    }

    public void finishedLoadingLogs() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (fragment == null || !(fragment instanceof GeoTagImagesFragment)) {
            fragment = new GeoTagImagesFragment();
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
    }

    public void finishedGeotagging() {
        finish();
    }

    public void updateTitle(@StringRes int title) {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarGeoTag = fm.findFragmentById(toolbarId);
        if (actionBarGeoTag != null && actionBarGeoTag instanceof ActionBarGeoTagFragment) {
            ((ActionBarGeoTagFragment) actionBarGeoTag).updateTitle(title);
        }
    }
}
