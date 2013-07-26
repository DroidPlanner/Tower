package com.droidplanner.widgets.HUD;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

class HudThread extends Thread {
	private SurfaceHolder _surfaceHolder;
	private HUD hud;
	private volatile boolean running = false;
	private Object dirty = new Object();

	public HudThread(SurfaceHolder surfaceHolder, HUD hud) {
		_surfaceHolder = surfaceHolder;
		this.hud = hud;
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
							hud.onDraw(c);
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