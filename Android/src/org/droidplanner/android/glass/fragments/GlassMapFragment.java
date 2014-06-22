package org.droidplanner.android.glass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import org.droidplanner.android.fragments.DroneMap;
import org.droidplanner.android.maps.providers.DPMapProvider;

/**
 * Used on glass to display the flight map data. Provides support for map control via glass
 * gestures.
 */
public class GlassMapFragment extends DroneMap {

    private static final String TAG = GlassMapFragment.class.getSimpleName();

    /**
     * How many different zooming level are supported.
     */
    private static final int MAP_ZOOM_PARTITIONS = 5;

    /**
     * Glass gesture detector used to provide map navigation via glass gestures.
     */
    protected GestureDetector mGestureDetector;

    /**
     * Zoom level limits for the underlying map.
     */
    protected float mMaxZoomLevel = -1;
    protected float mMinZoomLevel = -1;
    protected float mZoomLevelRange = -1;
    protected float mZoomStep = -1;

    /**
     * Used to track user head orientation. This is enabled when the user holds two fingers on
     * the touchpad, and is used to allow panning of the underlying map.
     */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

        mGestureDetector = new GestureDetector(context);
        mGestureDetector
                .setBaseListener(new GestureDetector.BaseListener() {
                    @Override
                    public boolean onGesture(Gesture gesture) {
                        if (mMinZoomLevel == -1) {
                            mMinZoomLevel = mMapFragment.getMinZoomLevel();
                        }

                        if (mMaxZoomLevel == -1) {
                            mMaxZoomLevel = mMapFragment.getMaxZoomLevel();
                        }

                        if (mZoomLevelRange == -1) {
                            mZoomLevelRange = mMaxZoomLevel - mMinZoomLevel;
                            mZoomStep = mZoomLevelRange / MAP_ZOOM_PARTITIONS;
                        }

                        switch (gesture) {
                            case SWIPE_RIGHT: {
                                updateMapZoomLevel(mMapFragment.getMapZoomLevel() + mZoomStep);
                                return true;
                            }

                            case SWIPE_LEFT: {
                                updateMapZoomLevel(mMapFragment.getMapZoomLevel() - mZoomStep);
                                return true;
                            }
                        }
                        return false;
                    }
                });
    }

    private float clampZoomLevel(float zoomLevel) {
        if (zoomLevel < mMinZoomLevel) {
            return mMinZoomLevel;
        }
        else if (zoomLevel > mMaxZoomLevel) {
            return mMaxZoomLevel;
        }
        return zoomLevel;
    }

    private void updateMapZoomLevel(float newZoomLevel) {
        final float clampedZoom = clampZoomLevel(newZoomLevel);
        mMapFragment.updateCamera(mMapFragment.getMapCenter(), clampedZoom);
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected DPMapProvider getMapProvider() {
        return DPMapProvider.MAPBOX;
    }

    @Override
    protected boolean isAutoPanEnabled() {
        return true;
    }
}
