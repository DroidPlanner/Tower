package org.droidplanner.android.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.actionbar.ActionBarGeoTagFragment;
import org.droidplanner.android.fragments.geotag.GeoTagImagesFragment;
import org.droidplanner.android.fragments.geotag.GetCameraLogsFragment;
import org.droidplanner.android.fragments.geotag.UploadImagesFragment;

import java.io.File;
import java.util.Collection;

/**
 * Created by chavi on 10/15/15.
 */
public class GeoTagActivity extends DrawerNavigationUI {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geotag);

//        finishedGeotagging(null);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (fragment == null) {
            fragment = new GetCameraLogsFragment();
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
        updateTitle(R.string.transfer_photo_label);

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
        updateTitle(R.string.geo_tag_label);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (fragment == null || !(fragment instanceof GeoTagImagesFragment)) {
            fragment = new GeoTagImagesFragment();
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, fragment).commit();
        }
    }

    public void finishedGeotagging(Collection<File> geotaggedList) {
        updateTitle(R.string.upload_images);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_geotag_layout);
        if (fragment == null || !(fragment instanceof UploadImagesFragment)) {
            UploadImagesFragment uploadImagesFragment = new UploadImagesFragment();
            uploadImagesFragment.setFileList(geotaggedList);
            fm.beginTransaction().replace(R.id.fragment_geotag_layout, uploadImagesFragment).commit();
        }
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
