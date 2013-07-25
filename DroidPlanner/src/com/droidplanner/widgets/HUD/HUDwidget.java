package com.droidplanner.widgets.HUD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder): - Improved consistency across
 * different screen sizes by replacing all fixed and density scaled size and
 * position values by percentual scaled values - Added functionality to show
 * dummy data for debugging purposes - Some minor layout changes
 */

public class HUDwidget extends SurfaceView implements SurfaceHolder.Callback,
		HudUpdatedListner {
	// in relation to attHeightPx
	private static final float ATT_FACTOR_INFOTEXT = .048f;
	// in relation to the resulting size of ATT_FACTOR_INFOTEXT
	private static final float ATT_FACTOR_INFOTEXT_Y_OFFSET = -.1f;
	// in relation to width
	private static final float ATT_FACTOR_INFOTEXT_X_OFFSET = .013f;
	// in relation to attSizePxInfoText
	private static final float ATT_FACTOR_INFOTEXT_CLEARANCE = .1f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_BORDER_WIDTH = .0075f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_RED_INDICATOR_WIDTH = .0075f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH = .005f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH = .0025f;
	// in relation to averaged of width and height
	private static final float HUD_FACTOR_CENTER_INDICATOR_SIZE = .0375f;
	// in relation to width
	private static final float FAILSAFE_FACTOR_TEXT = .093f;
	// in relation to the resulting size of FAILSAFE_FACTOR_TEXT
	private static final float FAILSAFE_FACTOR_BOX_PADDING = .27f;

	private ScopeThread renderer;
	int width;
	int height;
	HudAtt data = new HudAtt();
	private HudScroller hudScroller = new HudScroller();
	HudYaw hudYaw = new HudYaw();
	private HurRoll hudRoll = new HurRoll();
	private HudPitch hudPitch = new HudPitch();
	private int hudCenterIndicatorRadius;
	private int failsafeSizePxBoxPadding;

	private int armedCounter = 0;
	// private DisplayMetrics hudMetrics;

	static final boolean hudDebug = false;
	// hudDebug is the main switch for HUD debugging
	// |->false: Normal HUD operation.
	// '->true: HUD shows only the following dummy data! NO NORMAL OPERATION
	static final double hudDebugYaw = 42;
	private static final double hudDebugRoll = 45;
	private static final double hudDebugPitch = 11;
	static final double hudDebugGroundSpeed = 4.3;
	static final double hudDebugAirSpeed = 3.2;
	static final double hudDebugTargetSpeed = 3;
	static final double hudDebugAltitude = 8;
	static final double hudDebugTargetAltitude = 20;
	static final double hudDebugVerticalSpeed = 2.5;
	private static final double hudDebugBattRemain = 51;
	private static final double hudDebugBattCurrent = 40.5;
	private static final double hudDebugBattVolt = 12.32;
	private static final int hudDebugSatCount = 8;
	private static final int hudDebugFixType = 3;
	private static final double hudDebugGpsEPH = 2.4;
	private static final String hudDebugModeName = "Loiter";
	private static final int hudDebugWpNumber = 4;
	private static final double hudDebugDistToWp = 30.45;
	private static final int hudDebugDroneType = 2;
	private static final boolean hudDebugDroneArmed = false;

	// Paints
	Paint yawBg = new Paint();
	Paint whiteStroke = new Paint();
	Paint whiteBorder = new Paint();
	Paint whiteThickTics = new Paint();
	Paint whiteThinTics = new Paint();
	Paint FailsafeText = new Paint();
	Paint attInfoText = new Paint();
	Paint plane = new Paint();
	Paint blackSolid = new Paint();
	Paint blueVSI = new Paint();
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
		drawPitch(canvas);
		hudRoll.drawRoll(this, canvas);
		hudYaw.drawYaw(this, canvas);
		drawPlane(canvas);
		hudScroller.drawRightScroller(this, canvas);
		hudScroller.drawLeftScroller(this, canvas);
		drawAttitudeInfoText(canvas);
		drawFailsafe(canvas);
	}

	public HUDwidget(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);
		// hudMetrics = context.getResources().getDisplayMetrics();

		
		yawBg.setARGB(255, 0, 0, 0);// (64, 255, 255, 255);

		FailsafeText.setTextSize(37);
		FailsafeText.setAntiAlias(true);

		whiteStroke.setColor(Color.WHITE);
		whiteStroke.setStyle(Style.STROKE);
		whiteStroke.setStrokeWidth(3);
		whiteStroke.setAntiAlias(true);

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

		attInfoText.setColor(Color.WHITE);
		attInfoText.setTextAlign(Align.CENTER);
		attInfoText.setAntiAlias(true);

		plane.setColor(Color.RED);
		plane.setStyle(Style.STROKE);
		plane.setStrokeWidth(3);
		plane.setAntiAlias(true);

		blackSolid.setColor(Color.BLACK);
		blackSolid.setAntiAlias(true);
		blueVSI.setARGB(255, 0, 50, 250);
		blueVSI.setAntiAlias(true);
	}

	private void drawPlane(Canvas canvas) {
		canvas.drawCircle(0, 0, hudCenterIndicatorRadius, plane);
		canvas.drawLine(-hudCenterIndicatorRadius, 0,
				-hudCenterIndicatorRadius * 2, 0, plane);
		canvas.drawLine(hudCenterIndicatorRadius, 0,
				hudCenterIndicatorRadius * 2, 0, plane);
		canvas.drawLine(0, -hudCenterIndicatorRadius, 0,
				-hudCenterIndicatorRadius * 2, plane);
	}

	private void drawPitch(Canvas canvas) {
		double pitch = drone.orientation.getPitch();
		double roll = drone.orientation.getRoll();

		if (hudDebug) {
			pitch = hudDebugPitch;
			roll = hudDebugRoll;
		}

		int pitchOffsetPx = (int) (pitch * hudPitch.pitchPixPerDegree);
		int rollTriangleBottom = -data.attHeightPx / 2
				+ hudRoll.rollTopOffsetPx / 2 + hudRoll.rollTopOffsetPx;

		canvas.rotate(-(int) roll);

		// Draw the background
		canvas.drawRect(-width, pitchOffsetPx, width,
				2 * height /* Go plenty low */, hudPitch.ground);
		canvas.drawRect(-width, -2 * height /* Go plenty high */, width,
				pitchOffsetPx, hudPitch.sky);
		canvas.drawLine(-width, pitchOffsetPx, width, pitchOffsetPx,
				whiteThinTics);

		// Draw roll triangle
		Path arrow = new Path();
		int tempOffset = Math.round(plane.getStrokeWidth()
				+ whiteBorder.getStrokeWidth() / 2);
		arrow.moveTo(0, -data.attHeightPx / 2 + hudRoll.rollTopOffsetPx
				+ tempOffset);
		arrow.lineTo(0 - hudRoll.rollTopOffsetPx / 3, rollTriangleBottom
				+ tempOffset);
		arrow.lineTo(0 + hudRoll.rollTopOffsetPx / 3, rollTriangleBottom
				+ tempOffset);
		arrow.close();
		canvas.drawPath(arrow, plane);

		// Draw gauge
		int yPos;
		for (int i = -180; i <= 180; i += 5) {
			yPos = Math.round(-i * hudPitch.pitchPixPerDegree + pitchOffsetPx);
			if ((yPos < -rollTriangleBottom) && (yPos > rollTriangleBottom)
					&& (yPos != pitchOffsetPx)) {
				if (i % 2 == 0) {
					canvas.drawLine(-hudPitch.pitchScaleWideHalfWidth, yPos,
							hudPitch.pitchScaleWideHalfWidth, yPos,
							whiteThinTics);
					canvas.drawText(i + "", -hudPitch.pitchScaleWideHalfWidth
							- hudPitch.pitchScaleTextXOffset, yPos
							- hudPitch.pitchTextCenterOffsetPx, hudPitch.pitchText);
				} else
					canvas.drawLine(-hudPitch.pitchScaleNarrowHalfWidth, yPos,
							hudPitch.pitchScaleNarrowHalfWidth, yPos,
							whiteThinTics);
			}
		}

		canvas.rotate((int) roll);
	}

	private void drawAttitudeInfoText(Canvas canvas) {
		double battVolt = drone.battery.getBattVolt();
		double battCurrent = drone.battery.getBattCurrent();
		double battRemain = drone.battery.getBattRemain();
		double groundSpeed = drone.speed.getGroundSpeed();
		double airSpeed = drone.speed.getAirSpeed();
		int satCount = drone.GPS.getSatCount();
		int fixType = drone.GPS.getFixType();
		String modeName = drone.state.getMode().getName();
		int wpNumber = drone.mission.getWpno();
		double distToWp = drone.mission.getDisttowp();
		double gpsEPH = drone.GPS.getGpsEPH();

		if (hudDebug) {
			battVolt = hudDebugBattVolt;
			battCurrent = hudDebugBattCurrent;
			battRemain = hudDebugBattRemain;
			groundSpeed = hudDebugGroundSpeed;
			airSpeed = hudDebugAirSpeed;
			satCount = hudDebugSatCount;
			fixType = hudDebugFixType;
			modeName = hudDebugModeName;
			wpNumber = hudDebugWpNumber;
			distToWp = hudDebugDistToWp;
			gpsEPH = hudDebugGpsEPH;
		}

		// Left Top Text
		attInfoText.setTextAlign(Align.LEFT);

		if ((battVolt >= 0) || (battRemain >= 0))
			canvas.drawText(
					String.format("%2.1fV  %.0f%%", battVolt, battRemain),
					-width / 2 + data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextUpperTop, attInfoText);
		if (battCurrent >= 0)
			canvas.drawText(String.format("%2.1fA", battCurrent), -width / 2
					+ data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextUpperBottom, attInfoText);

		// Left Bottom Text
		canvas.drawText(String.format("AS %.1fms", airSpeed), -width / 2
				+ data.attPosPxInfoTextXOffset, data.attPosPxInfoTextLowerTop,
				attInfoText);
		canvas.drawText(String.format("GS %.1fms", groundSpeed), -width / 2
				+ data.attPosPxInfoTextXOffset,
				data.attPosPxInfoTextLowerBottom, attInfoText);

		// Right Top Text
		attInfoText.setTextAlign(Align.RIGHT);

		String gpsFix = "";
		if (satCount >= 0) {
			switch (fixType) {
			case 2:
				gpsFix = ("GPS2D(" + satCount + ")");
				break;
			case 3:
				gpsFix = ("GPS3D(" + satCount + ")");
				break;
			default:
				gpsFix = ("NoGPS(" + satCount + ")");
				break;
			}
		}
		canvas.drawText(gpsFix, width / 2 - data.attPosPxInfoTextXOffset,
				data.attPosPxInfoTextUpperTop, attInfoText);
		if (gpsEPH >= 0)
			canvas.drawText(String.format("hp%.1fm", gpsEPH), width / 2
					- data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextUpperBottom, attInfoText);

		// Right Bottom Text
		canvas.drawText(modeName, width / 2 - data.attPosPxInfoTextXOffset,
				data.attPosPxInfoTextLowerTop, attInfoText);
		if (wpNumber >= 0)
			canvas.drawText(String.format("%.0fm>WP#%d", distToWp, wpNumber),
					width / 2 - data.attPosPxInfoTextXOffset,
					data.attPosPxInfoTextLowerBottom, attInfoText);
	}

	private void drawFailsafe(Canvas canvas) {
		int droneType = drone.type.getType();
		boolean isArmed = drone.state.isArmed();

		if (hudDebug) {
			droneType = hudDebugDroneType;
			isArmed = hudDebugDroneArmed;
		}

		if (ApmModes.isCopter(droneType)) {
			if (isArmed) {
				if (armedCounter < 50) {
					FailsafeText.setColor(Color.RED);
					String text = "ARMED";
					Rect textRec = new Rect();
					FailsafeText.getTextBounds(text, 0, text.length(), textRec);
					textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
					RectF boxRec = new RectF(textRec.left
							- failsafeSizePxBoxPadding, textRec.top
							- failsafeSizePxBoxPadding, textRec.right
							+ failsafeSizePxBoxPadding, textRec.bottom
							+ failsafeSizePxBoxPadding);
					canvas.drawRoundRect(boxRec, failsafeSizePxBoxPadding,
							failsafeSizePxBoxPadding, blackSolid);
					canvas.drawText(text, textRec.left - 3, textRec.bottom - 1,
							FailsafeText);
					armedCounter++;
				}
			} else {
				FailsafeText.setColor(Color.GREEN);
				String text = "DISARMED";
				Rect textRec = new Rect();
				FailsafeText.getTextBounds(text, 0, text.length(), textRec);
				textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
				RectF boxRec = new RectF(textRec.left
						- failsafeSizePxBoxPadding, textRec.top
						- failsafeSizePxBoxPadding, textRec.right
						+ failsafeSizePxBoxPadding, textRec.bottom
						+ failsafeSizePxBoxPadding);
				canvas.drawRoundRect(boxRec, failsafeSizePxBoxPadding,
						failsafeSizePxBoxPadding, blackSolid);
				canvas.drawText(text, textRec.left - 3, textRec.bottom - 1,
						FailsafeText);
				armedCounter = 0;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		int tempAttTextClearance;
		int tempSize, tempOffset, tempAttSizeText;
		float hudScaleThickTicStrokeWidth, hudScaleThinTicStrokeWidth, hudBorderWidth, redIndicatorWidth;

		this.width = width;
		this.height = height;

		// do as much precalculation as possible here because it
		// takes some load off the onDraw() routine which is called much more
		// frequently

		hudCenterIndicatorRadius = Math.round((this.width + this.height) / 2
				* HUD_FACTOR_CENTER_INDICATOR_SIZE);

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
		plane.setStrokeWidth(redIndicatorWidth);

		hudYaw.setupYaw(this, this);

		data.attHeightPx = this.height - hudYaw.yawHeightPx;
		tempAttSizeText = Math.round(data.attHeightPx * ATT_FACTOR_INFOTEXT);
		attInfoText.setTextSize(tempAttSizeText);
		tempOffset = Math.round(tempAttSizeText * ATT_FACTOR_INFOTEXT_Y_OFFSET);
		tempAttTextClearance = Math.round(tempAttSizeText
				* ATT_FACTOR_INFOTEXT_CLEARANCE);
		data.attPosPxInfoTextXOffset = Math.round(this.width
				* ATT_FACTOR_INFOTEXT_X_OFFSET);

		tempAttTextClearance = hudScroller.setupScroller(this,
				tempAttTextClearance, tempAttSizeText);

		data.attPosPxInfoTextUpperTop = -data.attHeightPx / 2 + tempAttSizeText
				+ tempOffset + tempAttTextClearance;
		data.attPosPxInfoTextUpperBottom = -data.attHeightPx / 2 + 2
				* tempAttSizeText + tempOffset + 2 * tempAttTextClearance;
		data.attPosPxInfoTextLowerBottom = data.attHeightPx / 2 + tempOffset
				- tempAttTextClearance;
		data.attPosPxInfoTextLowerTop = data.attHeightPx / 2 - tempAttSizeText
				+ tempOffset - 2 * tempAttTextClearance;

		hudRoll.setupRoll(this);

		hudPitch.setupPitch(this);

		tempSize = Math.round(this.width * FAILSAFE_FACTOR_TEXT);
		FailsafeText.setTextSize(tempSize);
		failsafeSizePxBoxPadding = Math.round(tempSize
				* FAILSAFE_FACTOR_BOX_PADDING);
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
		private HUDwidget scope;
		private volatile boolean running = false;
		private Object dirty = new Object();

		public ScopeThread(SurfaceHolder surfaceHolder, HUDwidget panel) {
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
