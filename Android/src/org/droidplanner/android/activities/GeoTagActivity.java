package org.droidplanner.android.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.actionbar.ActionBarGeoTagFragment;
import org.droidplanner.android.fragments.geotag.FinishGeoTagFragment;
import org.droidplanner.android.fragments.geotag.GeoTagImagesFragment;
import org.droidplanner.android.fragments.geotag.GetCameraLogsFragment;

import java.io.File;
import java.util.ArrayList;

/**
 * Activity that handles UI for the geotagging images flow.
 */
public class GeoTagActivity extends DrawerNavigationUI {
    public static final String NUM_IMAGE_FILES = "numImageFiles";
    public static final String PARENT_DIR = "parentDir";

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
        if (!(fragment instanceof GeoTagImagesFragment)) {
            fragment = new GeoTagImagesFragment();
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
    }

    public void finishedGeotagging(ArrayList<File> files) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (!(fragment instanceof FinishGeoTagFragment)) {
            fragment = new FinishGeoTagFragment();
            Bundle args = new Bundle();
            args.putInt(NUM_IMAGE_FILES, files.size());
            if (files != null && files.size() > 0) {
                args.putString(PARENT_DIR, files.get(0).getParent());
            }
            fragment.setArguments(args);
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
    }

    public void updateTitle(@StringRes int title) {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarGeoTag = fm.findFragmentById(toolbarId);
        if (actionBarGeoTag instanceof ActionBarGeoTagFragment) {
            ((ActionBarGeoTagFragment) actionBarGeoTag).updateTitle(title);
        }
    }
}
