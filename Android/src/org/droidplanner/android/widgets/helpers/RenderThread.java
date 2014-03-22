package org.droidplanner.android.widgets.helpers;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class RenderThread extends Thread {
	public interface canvasPainter {
		public void onDraw(Canvas c);
	}

	private SurfaceHolder _surfaceHolder;
	private canvasPainter painter;
	private volatile boolean running = false;
	private Object dirty = new Object();

	public RenderThread(SurfaceHolder surfaceHolder, canvasPainter painter) {
		_surfaceHolder = surfaceHolder;
		this.painter = painter;
	}

	public boolean isRunning() {
		return running;

	}

	public void setRunning(boolean run) {
		running = run;
		setDirty();
	}

	/** We may need to redraw */
	public void setDirty() {
		synchronized (dirty) {
			dirty.notify();
		}
	}

	@SuppressLint("WrongCall")
	// TODO fix error
	@Override
	public void run() {
		Canvas c;
		while (running) {
			synchronized (dirty) {
				c = null;
				try {
					c = _surfaceHolder.lockCanvas(null);
					synchronized (_surfaceHolder) {
						if (c != null) {
							painter.onDraw(c);
						}
					}
				} finally {
					// do this in a finally so that if an exception is
					// thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						_surfaceHolder.unlockCanvasAndPost(c);
					}
				}

				// We do this wait at the _end_ to ensure we always draw at
				// least one frame of
				// HUD data
				try {
					// Log.d("HUD", "Waiting for change");
					dirty.wait(); // TODO - not quite ready
					// Log.d("HUD", "Handling change");
				} catch (InterruptedException e) {
					// We will try again and again
				}
			}
		}
	}
}