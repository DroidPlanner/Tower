package org.droidplanner.android.utils.ar.rendering;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import org.droidplanner.android.utils.tracker.TrackerUI;


/**
 * Tracker View
 */
public class TrackerSurfaceView extends GLSurfaceView {
    private TrackerDroneCameraRenderer renderer;
    TrackerUI ui = new TrackerUI();

    public TrackerSurfaceView(Context context) {
        super(context);

        // Request an OpenGL ES 2.0 compatible context.
        setEGLContextClientVersion(2);

        // Assign the renderer.
        renderer = new TrackerDroneCameraRenderer(context, this);
        setRenderer(renderer);

        // The renderer only renders when the surface is created, or when requestRender() is called.
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void onResume() {
        super.onResume();

        renderer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        renderer.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // @Tracker interface
        if (ui.onTouchEvent(e, renderer))
            requestRender();

        return true;
    }

    public TrackerDroneCameraRenderer getRenderer() {
        return renderer;
    }
}
