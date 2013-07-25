package com.droidplanner.widgets.HUD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder): - Improved consistency across
 * different screen sizes by replacing all fixed and density scaled size and
 * position values by percentual scaled values - Added functionality to show
 * dummy data for debugging purposes - Some minor layout changes
 */

public class HUD extends SurfaceView implements SurfaceHolder.Callback,
		HudUpdatedListner {
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_BORDER_WIDTH = .0075f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_RED_INDICATOR_WIDTH = .0075f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH = .005f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH = .0025f;

	private ScopeThread renderer;
	int width;
	int height;
	public HudYaw hudYaw = new HudYaw();
	public HudInfo data = new HudInfo();
	public HurRoll hudRoll = new HurRoll();
	public HudPlane hudPlane = new HudPlane();
	private HudPitch hudPitch = new HudPitch();
	public HudScroller hudScroller = new HudScroller();
	private HudFailsafe hudFailsafe = new HudFailsafe();

	static final boolean hudDebug = false;
	// hudDebug is the main switch for HUD debugging
	// |->false: Normal HUD operation.
	// '->true: HUD shows only the following dummy data! NO NORMAL OPERATION
	static final double hudDebugYaw = 42;
	static final double hudDebugRoll = 45;
	static final double hudDebugPitch = 11;
	static final double hudDebugGroundSpeed = 4.3;
	static final double hudDebugAirSpeed = 3.2;
	static final double hudDebugTargetSpeed = 3;
	static final double hudDebugAltitude = 8;
	static final double hudDebugTargetAltitude = 20;
	static final double hudDebugVerticalSpeed = 2.5;
	static final double hudDebugBattRemain = 51;
	static final double hudDebugBattCurrent = 40.5;
	static final double hudDebugBattVolt = 12.32;
	static final int hudDebugSatCount = 8;
	static final int hudDebugFixType = 3;
	static final double hudDebugGpsEPH = 2.4;
	static final String hudDebugModeName = "Loiter";
	static final int hudDebugWpNumber = 4;
	static final double hudDebugDistToWp = 30.45;
	static final int hudDebugDroneType = 2;
	static final boolean hudDebugDroneArmed = false;

	Paint whiteBorder = new Paint();
	Paint whiteThickTics = new Paint();
	Paint whiteThinTics = new Paint();
	Paint blackSolid = new Paint();
	Drone drone;

	@Override
	protected void onDraw(Canvas canvas) {
		if (drone == null) {
			return;
		}

		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));
		canvas.translate(width / 2, data.attHeightPx / 2 + hudYaw.yawHeightPx); // set
		// center of
		// HUD
		// excluding
		// YAW area

		// from now on each drawing routine has to undo all applied
		// transformations, clippings, etc by itself
		// this will improve performance because not every routine applies that
		// stuff, so general save and restore
		// is not necessary
		hudPitch.drawPitch(this, canvas);
		hudRoll.drawRoll(this, canvas);
		hudYaw.drawYaw(this, canvas);
		hudPlane.drawPlane(this, canvas);
		hudScroller.drawRightScroller(this, canvas);
		hudScroller.drawLeftScroller(this, canvas);
		data.drawAttitudeInfoText(this, canvas);
		hudFailsafe.drawFailsafe(this, canvas);
	}

	public HUD(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);

		whiteBorder.setColor(Color.WHITE);
		whiteBorder.setStyle(Style.STROKE);
		whiteBorder.setStrokeWidth(3);
		whiteBorder.setAntiAlias(true);

		whiteThinTics.setColor(Color.WHITE);
		whiteThinTics.setStyle(Style.FILL);
		whiteThinTics.setStrokeWidth(1);
		whiteThinTics.setAntiAlias(true);

		whiteThickTics.setColor(Color.WHITE);
		whiteThickTics.setStyle(Style.FILL);
		whiteThickTics.setStrokeWidth(2);
		whiteThickTics.setAntiAlias(true);

		data.attInfoText.setColor(Color.WHITE);
		data.attInfoText.setTextAlign(Align.CENTER);
		data.attInfoText.setAntiAlias(true);

		hudPlane.plane.setColor(Color.RED);
		hudPlane.plane.setStyle(Style.STROKE);
		hudPlane.plane.setStrokeWidth(3);
		hudPlane.plane.setAntiAlias(true);

		blackSolid.setColor(Color.BLACK);
		blackSolid.setAntiAlias(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		float hudScaleThickTicStrokeWidth, hudScaleThinTicStrokeWidth, hudBorderWidth, redIndicatorWidth;

		this.width = width;
		this.height = height;

		// do as much precalculation as possible here because it
		// takes some load off the onDraw() routine which is called much more
		// frequently

		hudPlane.setupPlane(this);

		hudScaleThickTicStrokeWidth = (this.width + this.height) / 2
				* HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH;
		if (hudScaleThickTicStrokeWidth < 1)
			hudScaleThickTicStrokeWidth = 1;
		whiteThickTics.setStrokeWidth(hudScaleThickTicStrokeWidth);

		hudScaleThinTicStrokeWidth = (this.width + this.height) / 2
				* HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH;
		if (hudScaleThinTicStrokeWidth < 1)
			hudScaleThinTicStrokeWidth = 1;
		whiteThinTics.setStrokeWidth(hudScaleThinTicStrokeWidth);

		hudBorderWidth = (this.width + this.height) / 2
				* HUD_FACTOR_BORDER_WIDTH;
		if (hudBorderWidth < 1)
			hudBorderWidth = 1;
		whiteBorder.setStrokeWidth(hudBorderWidth);

		redIndicatorWidth = (this.width + this.height) / 2
				* HUD_FACTOR_RED_INDICATOR_WIDTH;
		if (redIndicatorWidth < 1)
			redIndicatorWidth = 1;
		hudPlane.plane.setStrokeWidth(redIndicatorWidth);

		hudYaw.setupYaw(this, this);

		data.setupAtt(this);

		hudRoll.setupRoll(this);

		hudPitch.setupPitch(this);

		hudFailsafe.setupFailsafe(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderer = new ScopeThread(getHolder(), this);
		if (!renderer.isRunning()) {
			renderer.setRunning(true);
			renderer.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		renderer.setRunning(false);
		while (retry) {
			try {
				renderer.join();
				renderer = null;
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	private class ScopeThread extends Thread {
		private SurfaceHolder _surfaceHolder;
		private HUD scope;
		private volatile boolean running = false;
		private Object dirty = new Object();

		public ScopeThread(SurfaceHolder surfaceHolder, HUD panel) {
			_surfaceHolder = surfaceHolder;
			scope = panel;
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
								scope.onDraw(c);
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

	public void setDrone(Drone drone) {
		this.drone = drone;
		this.drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		if (renderer != null)
			renderer.setDirty();
	}
}
