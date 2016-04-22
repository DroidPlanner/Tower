package org.droidplanner.android.utils.ar.rendering;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

/**
 * Augmented reality OpenGL view.
 */
public class ARSurfaceView extends GLSurfaceView {
    private ARDroneCameraRenderer renderer;

    public ARSurfaceView(Context context) {
        super(context);

        // Request an OpenGL ES 2.0 compatible context.
        setEGLContextClientVersion(2);

        // Assign the renderer.
        renderer = new ARDroneCameraRenderer(context, this);
        setRenderer(renderer);

        // The renderer only renders when the surface is created, or when requestRender() is called.
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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

    public ARDroneCameraRenderer getRenderer() {
        return renderer;
    }
}
