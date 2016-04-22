package org.droidplanner.android.utils.tracker;

import android.view.MotionEvent;

import timber.log.Timber;

/**
 * Created by Aaron Licata on 2/17/2016.
 */
public class TrackerUI {

    private float mPreviousX;
    private float mPreviousY;

    private float startX;
    private float startY;

    public boolean onTouchEvent(MotionEvent e, TrackerUiInterface renderer) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        boolean requestRender = false;

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Timber.d("TrackerTLD DOWN at startXY: %f, %f", x, y);
                startX = x;
                startY = y;

            case MotionEvent.ACTION_UP:
                //float dx = x - mPreviousX;
                //float dy = y - mPreviousY;
                Timber.d("TrackerTLD UP at : %f, %f", x, y);
                if (x > startX && y > startY) {
                    float dx = x - startX;
                    float dy = y - startY;
                    if (dx > 30.0 && dy > 30.0)
                        renderer.setBox(startX, startY, x - startX, y - startY, true);
                    else
                        renderer.setBox(startX, startY, x - startX, y - startY, false);
                }

                renderer.toggleCommands(x,y);

            case MotionEvent.ACTION_MOVE:
                //float dx = x - mPreviousX;
                //float dy = y - mPreviousY;
                Timber.d("TrackerTLD MOVE TO: %f, %f", x, y);
                if (x > startX && y > startY) {
                    renderer.setBox(startX, startY, x - startX, y - startY, false);
                requestRender = true;
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return requestRender;
    }

}
