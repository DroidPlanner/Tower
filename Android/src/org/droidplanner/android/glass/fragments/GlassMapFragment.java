package org.droidplanner.android.glass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

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
     * Scroll input is only registered once it crosses this threshold.
     */
    protected static final float SCROLL_DELTA_THRESHOLD = 10f;

    /**
     * Glass gesture detector used to provide map navigation via glass gestures.
     */
    protected GestureDetector mGestureDetector;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = getActivity().getApplicationContext();
        mGestureDetector = new GestureDetector(context);
        mGestureDetector
                .setFingerListener(new GestureDetector.FingerListener() {
                    @Override
                    public void onFingerCountChanged(int previousCount, int currentCount) {

                    }
                })
                .setScrollListener(new GestureDetector.ScrollListener() {
                    @Override
                    public boolean onScroll(float displacement, float delta, float velocity) {
                        return handleScroll(displacement, delta, velocity);
                    }
                })
                .setTwoFingerScrollListener(new GestureDetector.TwoFingerScrollListener() {
                    @Override
                    public boolean onTwoFingerScroll(float displacement, float delta,
                                                     float velocity) {
                        return handleScroll(displacement, delta, velocity);
                    }
                });
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected DPMapProvider getMapProvider() {
        return DPMapProvider.MAPBOX;
    }

    private boolean handleScroll(float displacement, float delta, float velocity){
        if(Math.abs(delta) > SCROLL_DELTA_THRESHOLD) {
            Log.d(TAG, "[displacement: " + displacement + ", delta: " + delta + ", " +
                    "velocity: " + velocity + "]");
        }
        return true;
    }
}
