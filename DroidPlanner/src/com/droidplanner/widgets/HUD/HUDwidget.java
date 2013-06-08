package com.droidplanner.widgets.HUD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.MAVLink.Drone.HudUpdatedListner;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder):
 * 		- Improved consistency across different screen sizes and densities.
 * 		- Added functionality to show dummy data for debugging purposes
 */

public class HUDwidget extends SurfaceView implements SurfaceHolder.Callback, HudUpdatedListner {
	private static final float 	SCROLLER_WIDTH_FACTOR = .15f;			//in relation to width (total hud widget width)
	private static final float  SCROLLER_MAX_HEIGHT_FACTOR = .6f;		//in relation to attHeightPx
	private static final float		SCROLLER_FACTOR_TEXT = .045f;		//in relation to attHeightPx
	private static final float 		SCROLLER_FACTOR_ARROW_HEIGTH = 1.1f; //in relation to scrollerSizePxText
	private static final float 	ROLL_TOP_OFFSET_FACTOR = .05f;			//in relation to attHeightPx
	private static final float  	ROLL_FACTOR_TEXT = 0.030f;			//in relation to attHeightPx
	private static final float	PITCH_FACTOR_TEXT = 0.030f;				//in relation to attHeightPx
	
	private static final float	YAW_HEIGHT_FACTOR = .04f;				//in relation to height (total hud widget height)
	private static final float		YAW_FACTOR_TEXT = .80f;				//in relatio to yawHeightPx
	private static final float		YAW_FACTOR_TEXT_Y_OFFSET = -.10f;	//in relation to yawSizePxText	
	private static final float		YAW_FACTOR_TICS_SMALL = .15f;		//in relation to yawHeightPx
	private static final float		YAW_FACTOR_TICS_TALL = .30f;		//in relation to yawHeightPx
	private static final int 	YAW_PIXEL_PER_DEGREE = 6;
	private static final float	ATT_FACTOR_INFOTEXT = .035f;			//in relation to attHeightPx
	private static final float	ATT_FACTOR_INFOTEXT_CLEARANCE = .3f; 	//in relation to attSizePxInfoText

	private static final int SCROLLER_VSI_RANGE = 12;
	private static final int SCROLLER_ALT_RANGE = 26;
	private static final int SCROLLER_SPEED_RANGE = 26;
	
	private static final float PLANE_PAINT_STROKEWIDTH = 3;
	
	private static final int FAILSAFE_TEXT_SIZE = 50 ;
	private static final int FAILSAFE_BOX_PADDING = 10;
	

	private ScopeThread renderer;
	private int width;
	private int height;
	private int attHeightPx;
	private int 	attSizePxInfoText;
	private int 	attPosPxInfoTextUpperTop;
	private int 	attPosPxInfoTextUpperBottom;
	private int 	attPosPxInfoTextLowerTop;
	private int 	attPosPxInfoTextLowerBottom;
	private int scrollerHeightPx;
	private int scrollerWidthPx;
	private int 	scrollerSizePxText;
	private int 	scrollerSizePxTextOffset;
	private int 	scrollerSizePxArrowHeight;
	private int yawHeightPx;
	private int		yawSizePxText;
	private int		yawSizePxTextYOffset;
	private int		yawNumDegreesToShow;
	private double	yawDegreesPerPixel;
	private int		yawSizePxTicsSmall;
	private int		yawSizePxTicsTall;
	private int rollTopOffsetPx;
	private int pitchTextCenterOffsetPx;


	private int armedCounter = 0;
	private float hudDensity = 1;
	
	private boolean hudDebug = false;
	//hudDebug is the main switch for HUD debugging
	// |->flase: Normal HUD operation.
	// '->true:  HUD shows only the following dummy data! NO NORMAL OPERATION
	private double 	hudDebugYaw = 42;
	private double 	hudDebugRoll = 45;
	private double 	hudDebugPitch = 11;
	private double 	hudDebugGroundSpeed = 4;
	private double 	hudDebugAirSpeed = 3;
	private double 	hudDebugTargetSpeed = 3;
	private double  hudDebugAltitude = 8;
	private double  hudDebugTargetAltitude = 20;
	private double  hudDebugVerticalSpeed = -3;
	private double 	hudDebugBattRemain = 51;
	private double 	hudDebugBattCurrent = 40.5;
	private double 	hudDebugBattVolt = 12.32;
	private int  	hudDebugSatCount = 8;
	private int 	hudDebugFixType = 3;
	private String  hudDebugModeName = "Loiter";
	private int 	hudDebugWpNumber = 4;
	private double  hudDebugDistToWp = 30.45;
	private int		hudDebugDroneType = 2;
	private boolean hudDebugDroneArmed = false;
	
	
	// Paints
	Paint ground = new Paint();
	Paint sky = new Paint();
	Paint yawBg = new Paint();
	Paint yawText = new Paint();
	Paint yawNumbers = new Paint();
	Paint yawScale = new Paint();
	Paint rollText = new Paint();
	Paint pitchText = new Paint();
	Paint pitchScale = new Paint();
	Paint scrollerBg = new Paint();
	Paint scrollerText = new Paint();
	Paint whiteStroke = new Paint();
	Paint whiteBorder = new Paint();
	Paint FailsafeText = new Paint();
	Paint attInfoText = new Paint();
	Paint plane = new Paint();
	Paint blackSolid = new Paint();
	Paint blueVSI = new Paint();
	Paint greenPen = new Paint();
	
	private Drone drone;
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (drone == null){
			return;
		}
		
		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));
		canvas.translate(width / 2, attHeightPx / 2 + yawHeightPx);		// set center of HUD excluding YAW area

		//from now on each drawing routine has to undo all applied transformations, clippings, etc by itself
		//this will improve performance because not every routine applies that stuff, so general save and restore 
		//is not necessary
		drawPitch(canvas);
		drawRoll(canvas);
		drawYaw(canvas);
		drawPlane(canvas);
		drawRightScroller(canvas);
		drawLeftScroller(canvas);
		drawAttitudeInfoText(canvas);
		drawFailsafe(canvas);
	}
	
	public HUDwidget(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);
		hudDensity = context.getResources().getDisplayMetrics().density;
		
		ground.setARGB(220, 148, 193, 31);
		sky.setARGB(220, 0, 113, 188);
		yawBg.setARGB(255, 0, 0, 0);//(64, 255, 255, 255);
		scrollerBg.setARGB(64, 255, 255, 255);//(255, 0, 0, 0);
		
		scrollerText.setColor(Color.WHITE);
		scrollerText.setAntiAlias(true);
	
		FailsafeText.setColor(Color.RED);
		FailsafeText.setTextSize(FAILSAFE_TEXT_SIZE);
		FailsafeText.setAntiAlias(true);

		whiteStroke.setColor(Color.WHITE);
		whiteStroke.setStyle(Style.STROKE);
		whiteStroke.setStrokeWidth(3);
		whiteStroke.setAntiAlias(true);
		
		whiteBorder.setColor(Color.WHITE);
		whiteBorder.setStyle(Style.STROKE);
		whiteBorder.setStrokeWidth(1.5f * hudDensity);
		whiteBorder.setAntiAlias(true);

		yawText.setColor(Color.WHITE);
		yawText.setFakeBoldText(true);
		yawText.setTextAlign(Align.CENTER);
		yawText.setAntiAlias(true);
		
		yawNumbers.setColor(Color.WHITE);
		yawNumbers.setTextAlign(Align.CENTER);
		yawNumbers.setAntiAlias(true);
		
		yawScale.setColor(Color.WHITE);
		yawScale.setStyle(Style.FILL);
		yawScale.setStrokeWidth(1.0f * hudDensity);
		
		rollText.setColor(Color.WHITE);
		rollText.setAntiAlias(true);
		rollText.setTextAlign(Align.CENTER);
		
		pitchText.setColor(Color.WHITE);
		pitchText.setAntiAlias(true);
		pitchText.setTextAlign(Align.RIGHT);
		
		pitchScale.setColor(Color.WHITE);
		pitchScale.setAntiAlias(true);
		pitchScale.setStrokeWidth(1.0f * hudDensity);
		
		attInfoText.setColor(Color.WHITE);
		attInfoText.setTextAlign(Align.CENTER);
		attInfoText.setAntiAlias(true);

		greenPen.setColor(Color.GREEN);
		greenPen.setStrokeWidth(6);
		greenPen.setStyle(Style.STROKE);

		plane.setColor(Color.RED);
		plane.setStyle(Style.STROKE);
		plane.setStrokeWidth(PLANE_PAINT_STROKEWIDTH * hudDensity);
		plane.setAntiAlias(true);

		blackSolid.setColor(Color.BLACK);
		blackSolid.setAntiAlias(true);
		blueVSI.setARGB(255, 0, 50, 250);
		blueVSI.setAntiAlias(true);
	}

	private void drawPlane(Canvas canvas) {
		canvas.drawCircle(0, 0, 15, plane);
		canvas.drawLine(-15, 0, -30, 0, plane);
		canvas.drawLine(15, 0, 30, 0, plane);
		canvas.drawLine(0, -15, 0, -30, plane);
	}

	private void drawYaw(Canvas canvas) {
		int yawBottom = -attHeightPx / 2;
		canvas.drawRect(-width / 2, yawBottom - yawHeightPx, width / 2, yawBottom, yawBg);
		canvas.drawLine(-width / 2, yawBottom, width / 2, yawBottom, whiteBorder);

		// width / 2 == yawPosition
		// then round to nearest 5 degrees, and draw it.
		double yaw = drone.getYaw();
		if (hudDebug)
			yaw = hudDebugYaw;
		
		double centerDegrees = yaw;

		double mod = yaw % 5;
		for (double angle = (centerDegrees - mod) - yawNumDegreesToShow / 2.0; 
				angle <= (centerDegrees - mod) + yawNumDegreesToShow / 2.0;
				angle += 5) {

			// protect from wraparound
			double workAngle = (angle + 360.0);
			while (workAngle >= 360)
				workAngle -= 360.0;

			// need to draw "angle"
			// How many pixels from center should it be?
			int distanceToCenter = (int) ((angle - centerDegrees) * yawDegreesPerPixel);

			if (workAngle % 45 == 0) {
				String compass[] = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
				int index = (int) workAngle / 45;
				canvas.drawLine(distanceToCenter, yawBottom - yawSizePxTicsSmall, distanceToCenter, yawBottom, yawScale);
				canvas.drawText(compass[index], distanceToCenter, yawBottom - yawHeightPx + yawSizePxText + yawSizePxTextYOffset, yawText);
			} else if (workAngle % 15 == 0) {
				canvas.drawLine(distanceToCenter, yawBottom - yawSizePxTicsSmall, distanceToCenter, yawBottom, yawScale);
				canvas.drawText((int) (workAngle) + "", distanceToCenter, yawBottom - yawHeightPx + yawSizePxText + yawSizePxTextYOffset, yawNumbers);
			} else {
				canvas.drawLine(distanceToCenter, yawBottom - yawSizePxTicsTall, distanceToCenter, yawBottom, yawScale);
			}
		}

		// Draw the center line
		canvas.drawLine(0, yawBottom - yawHeightPx, 0, yawBottom + 2 * hudDensity, plane);
	}

	private void drawRoll(Canvas canvas) {
		int r = Math.round(attHeightPx / 2 - rollTopOffsetPx);
		RectF rec = new RectF(-r, -r, r, r);
		
		//test rec
		//canvas.drawRect(rec, greenPen);
		
		// Draw the arc
		canvas.drawArc(rec, 225, 90, false, whiteStroke);

		// Draw center triangle
		Path arrow = new Path();
		arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx - PLANE_PAINT_STROKEWIDTH);
		arrow.lineTo(0 - rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 - PLANE_PAINT_STROKEWIDTH);
		arrow.lineTo(0 + rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 - PLANE_PAINT_STROKEWIDTH);
		arrow.close();
		canvas.drawPath(arrow, plane);
		
		// draw the ticks
		// The center of the circle is at: 0, 0
		for (int i = -45; i <= 45; i += 15) {
			if (i != 0) {
				// Draw ticks
				float dx = (float) Math.sin(i * Math.PI / 180) * r;
				float dy = (float) Math.cos(i * Math.PI / 180) * r;
				canvas.drawLine(dx, -dy, (dx + (dx / 25)), -(dy + dy / 25), whiteStroke);
				// Draw the labels
				dx = (float) Math.sin(i * Math.PI / 180) * (r + 10);
				dy = (float) Math.cos(i * Math.PI / 180) * (r + 10);
				canvas.drawText(Math.abs(i) + "", dx + (dx / 25), -(dy + dy / 25), rollText);

			}
		}
		
		//current roll angle will be drawn by drawPitch()
	}

	private void drawPitch(Canvas canvas) {
		double pitch = drone.getPitch();
		double roll = drone.getRoll();
		
		if (hudDebug) {
			pitch = hudDebugPitch;
			roll = hudDebugRoll;
		}
		
		float pxPerDegree = 8;
		int pitchOffsetPx = (int) (pitch * pxPerDegree);
		int rollTriangleBottom = -attHeightPx / 2 + rollTopOffsetPx / 2 + rollTopOffsetPx;
		
		canvas.rotate(-(int) roll);

		// Draw the background
		canvas.drawRect(-width, pitchOffsetPx, width, 2 * height /* Go plenty low */, ground);
		canvas.drawRect(-width, -2 * height /* Go plenty high */, width, pitchOffsetPx, sky);
		canvas.drawLine(-width, pitchOffsetPx, width, pitchOffsetPx, pitchScale);
		
		// Draw roll triangle
		Path arrow = new Path();
		arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx + PLANE_PAINT_STROKEWIDTH);
		arrow.lineTo(0 - rollTopOffsetPx / 3, rollTriangleBottom + PLANE_PAINT_STROKEWIDTH);
		arrow.lineTo(0 + rollTopOffsetPx / 3, rollTriangleBottom + PLANE_PAINT_STROKEWIDTH);
		arrow.close();
		canvas.drawPath(arrow, plane);
		
		// Draw gauge
		int yPos;
		for (int i = -180; i <= 180; i += 5) {
			yPos = Math.round(-i * pxPerDegree + pitchOffsetPx);
			if ((yPos < -rollTriangleBottom) && (yPos > rollTriangleBottom) && (yPos != pitchOffsetPx)) {
				if (i % 2 == 0) {
					canvas.drawLine(-50, yPos, 50, yPos, pitchScale);
					canvas.drawText(i + "", -60, yPos - pitchTextCenterOffsetPx, pitchText);
				}
				else
					canvas.drawLine(-20, yPos, 20, yPos, pitchScale);
			}
		}

		canvas.rotate((int) roll);
	}

	private void drawRightScroller(Canvas canvas) {
		final float textHalfSize = scrollerSizePxText / 2 - 1;
		scrollerText.setTextAlign(Paint.Align.LEFT);
		
		double altitude = drone.getAltitude();
		double targetAltitude = drone.getTargetAltitude();
		double verticalSpeed = drone.getVerticalSpeed();
		
		if (hudDebug) {
			altitude = hudDebugAltitude;
			targetAltitude = hudDebugTargetAltitude;
			verticalSpeed = hudDebugVerticalSpeed;
		}
			
		// Outside box
		RectF scroller = new RectF(width / 2 - scrollerWidthPx, -scrollerHeightPx / 2, width / 2, scrollerHeightPx / 2);
		
		// Draw Vertical speed indicator
		final float vsi_width = scroller.width() / 4;
		float linespace = scroller.height() / SCROLLER_VSI_RANGE;
		Path vsiBox = new Path();
		vsiBox.moveTo(scroller.left, scroller.top); // draw outside box
		vsiBox.lineTo(scroller.left - vsi_width, scroller.top + vsi_width);
		vsiBox.lineTo(scroller.left - vsi_width, scroller.bottom - vsi_width);
		vsiBox.lineTo(scroller.left, scroller.bottom);
		for (int a = 1; a < SCROLLER_VSI_RANGE; a++) { // draw ticks
			float lineHeight = scroller.top + linespace * a;
			vsiBox.moveTo(scroller.left - vsi_width, lineHeight);
			vsiBox.lineTo(scroller.left - vsi_width / 2, lineHeight);
		}
		float vsiFillTrim = 0;
		if (verticalSpeed > 1) { // TODO Vertical Speed indicator must be tested
			vsiFillTrim = -1;
		} else if (verticalSpeed < -1) {
			vsiFillTrim = 1;
		}

		Path vsiFill = new Path();
		vsiFill.moveTo(scroller.left, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width,
				(scroller.centerY() - (((float) verticalSpeed) + vsiFillTrim)
						* linespace));
		vsiFill.lineTo(scroller.left,
				(scroller.centerY() - ((float) verticalSpeed) * linespace));
		vsiFill.lineTo(scroller.left, scroller.centerY());
		canvas.drawPath(vsiFill, blueVSI);
		canvas.drawPath(vsiBox, whiteBorder);

		// Draw Altitude Scroller
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height() / (float) SCROLLER_ALT_RANGE;
		int start = ((int) altitude - SCROLLER_ALT_RANGE / 2);

		if (start > targetAltitude) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((altitude + SCROLLER_SPEED_RANGE / 2) < targetAltitude) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}
		
		float targetAltPos = Float.MIN_VALUE;
		for (int a = start; a <= (altitude + SCROLLER_ALT_RANGE / 2); a += 1) { // go trough 1m steps

			float lineHeight = scroller.centerY() - space * (a - (int) altitude);

			if (a == ((int) targetAltitude) && targetAltitude != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right, lineHeight, greenPen);
				targetAltPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.left + 10, lineHeight, whiteStroke);
				canvas.drawText(Integer.toString(a), scroller.left + 15, lineHeight + textHalfSize + scrollerSizePxTextOffset, scrollerText);
			}
		}

		// Arrow with current altitude
		int borderWidth = Math.round(whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.right, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4 + borderWidth, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + borderWidth, 0);
		arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4 + borderWidth, scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right, scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, blackSolid);
		if ((targetAltPos != Float.MIN_VALUE) && (targetAltPos > -scrollerSizePxArrowHeight / 2) && (targetAltPos < scrollerSizePxArrowHeight / 2)) {
			canvas.drawLine(scroller.left, targetAltPos, scroller.right, targetAltPos, greenPen);
		}
		canvas.drawPath(arrow, plane);
		canvas.drawText(Integer.toString((int) altitude), scroller.left + 15,
				textHalfSize + scrollerSizePxTextOffset, scrollerText);
		// Reset clipping of Scroller
		canvas.clipRect(-width / 2, -height / 2, width / 2, height / 2,Region.Op.REPLACE);
	}

	private void drawLeftScroller(Canvas canvas) {
		final float textHalfSize = scrollerSizePxText / 2 - 1;
		scrollerText.setTextAlign(Paint.Align.RIGHT);

		double groundSpeed = drone.getGroundSpeed();
		double airSpeed = drone.getAirSpeed();
		double targetSpeed = drone.getTargetSpeed();
		if (hudDebug) {
			groundSpeed = hudDebugGroundSpeed;
			airSpeed = hudDebugAirSpeed;
			targetSpeed = hudDebugTargetSpeed;
		}
		
		double speed = airSpeed; // TODO test airSpeed
		if (speed == 0)
			speed = groundSpeed;

		// Outside box
		RectF scroller = new RectF(-width / 2, -scrollerHeightPx / 2,
				-width / 2 + scrollerWidthPx, scrollerHeightPx / 2);
		
		// Draw Scroll
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height() / (float) SCROLLER_SPEED_RANGE;
		int start = ((int) speed - SCROLLER_SPEED_RANGE / 2);

		if (start > targetSpeed) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((speed + SCROLLER_SPEED_RANGE / 2) < targetSpeed) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}

		float targetSpdPos = Float.MIN_VALUE;
		for (int a = start; a <= (speed + SCROLLER_SPEED_RANGE / 2); a += 1) {
			float lineHeight = scroller.centerY() - space * (a - (int) speed);

			if (a == ((int) targetSpeed) && targetSpeed != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right, lineHeight, greenPen);
				targetSpdPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.right, lineHeight,
						scroller.right - 10, lineHeight, whiteStroke);
				canvas.drawText(Integer.toString(a), scroller.right - 15,
						lineHeight + textHalfSize + scrollerSizePxTextOffset, scrollerText);
			}
		}

		// Arrow with current speed
		int borderWidth = Math.round(whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.left - borderWidth, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4 - borderWidth, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - borderWidth, 0);
		arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4 - borderWidth, scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left - borderWidth, scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, blackSolid);
		if ((targetSpdPos != Float.MIN_VALUE) && (targetSpdPos > -scrollerSizePxArrowHeight / 2) && (targetSpdPos < scrollerSizePxArrowHeight / 2)) {
			canvas.drawLine(scroller.left, targetSpdPos, scroller.right, targetSpdPos, greenPen);
		}
		canvas.drawPath(arrow, plane);
		canvas.drawText(Integer.toString((int) speed), scroller.right - 15,
				textHalfSize + scrollerSizePxTextOffset, scrollerText);
		// Reset clipping of Scroller
		canvas.clipRect(-width / 2, -height / 2, width / 2, height / 2,Region.Op.REPLACE);
	}
	
	private void drawAttitudeInfoText(Canvas canvas) {
		double battVolt = drone.getBattVolt();
		double battCurrent = drone.getBattCurrent();
		double battRemain = drone.getBattRemain();
		double groundSpeed = drone.getGroundSpeed();
		double airSpeed = drone.getAirSpeed();
		int satCount = drone.getSatCount();
		int fixType = drone.getFixType();
		String modeName = drone.getMode().getName();
		int wpNumber = drone.getWpno();
		double distToWp = drone.getDisttowp();
		
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
		}
		
		// Left Top Text
		attInfoText.setTextAlign(Align.LEFT);
		
		if (battVolt >= 0)
			canvas.drawText(String.format("%2.2f V", battVolt), -width / 2 + 5, attPosPxInfoTextUpperTop, attInfoText);
		if (battCurrent >= 0)
			canvas.drawText(String.format("%2.1f A", battCurrent), -width / 2 + 5, attPosPxInfoTextUpperBottom, attInfoText);

		// Left Bottom Text	
		canvas.drawText("AS " + Integer.toString((int) airSpeed), -width / 2 + 5, attPosPxInfoTextLowerBottom, attInfoText);
		canvas.drawText("GS " + Integer.toString((int) groundSpeed), -width / 2 + 5, attPosPxInfoTextLowerTop, attInfoText);
		
		// Right Top Text
		attInfoText.setTextAlign(Align.RIGHT);
		
		String gpsFix = "NoGPS";
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
		canvas.drawText(gpsFix, width / 2 -5, attPosPxInfoTextUpperTop, attInfoText);
		if (battRemain >= 0)
			canvas.drawText(String.format("%3.0f%%",battRemain), width / 2 -5, attPosPxInfoTextUpperBottom, attInfoText);
		
		// Right Bottom Text
		canvas.drawText(modeName, width / 2 -5, attPosPxInfoTextLowerTop, attInfoText);
		canvas.drawText(String.format("%.0fm>WP#%d", distToWp, wpNumber),
				width / 2 -5, attPosPxInfoTextLowerBottom, attInfoText);	
	}
	
	private void drawFailsafe(Canvas canvas) {
		int droneType = drone.getType();
		boolean isArmed = drone.isArmed();
			
		if (hudDebug) {
			droneType = hudDebugDroneType;
			isArmed = hudDebugDroneArmed;
		}
		
		if (ApmModes.isCopter(droneType)) {
			if (isArmed) {
				if (armedCounter < 50) {
					String text = "ARMED";
					Rect textRec = new Rect();
					FailsafeText.getTextBounds(text, 0, text.length(), textRec);
					textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
					RectF boxRec = new RectF(textRec.left - FAILSAFE_BOX_PADDING, textRec.top - FAILSAFE_BOX_PADDING, textRec.right + FAILSAFE_BOX_PADDING, textRec.bottom + FAILSAFE_BOX_PADDING);
					canvas.drawRoundRect(boxRec, FAILSAFE_BOX_PADDING, FAILSAFE_BOX_PADDING, blackSolid);
					canvas.drawText(text, textRec.left -3, textRec.bottom -1, FailsafeText);
					armedCounter++;
				}
			} else {
				String text = "DISARMED";
				Rect textRec = new Rect();
				FailsafeText.getTextBounds(text, 0, text.length(), textRec);
				textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
				RectF boxRec = new RectF(textRec.left - FAILSAFE_BOX_PADDING, textRec.top - FAILSAFE_BOX_PADDING, textRec.right + FAILSAFE_BOX_PADDING, textRec.bottom + FAILSAFE_BOX_PADDING);
				canvas.drawRoundRect(boxRec, FAILSAFE_BOX_PADDING, FAILSAFE_BOX_PADDING, blackSolid);
				canvas.drawText(text, textRec.left -3, textRec.bottom -1, FailsafeText);
				armedCounter = 0;
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		FontMetricsInt tempMetrics;
		int tempClearance;
		int tempOffset;
		
		this.width = width;
		this.height = height;
		
		//do as much precalculation as possible here because it
		//takes some load off the onDraw() routine which is called much more frequently
		
		yawHeightPx = Math.round(this.height * YAW_HEIGHT_FACTOR * hudDensity);
		yawSizePxText = Math.round(yawHeightPx * YAW_FACTOR_TEXT);
		yawText.setTextSize(yawSizePxText);
		yawNumbers.setTextSize(yawSizePxText);
		yawSizePxTextYOffset = Math.round(yawSizePxText * YAW_FACTOR_TEXT_Y_OFFSET);
		yawSizePxTicsSmall = Math.round(yawHeightPx * YAW_FACTOR_TICS_SMALL);
		yawSizePxTicsTall = Math.round(yawHeightPx * YAW_FACTOR_TICS_TALL);
		
		int pixelPerDegree = Math.round(YAW_PIXEL_PER_DEGREE * hudDensity);	
		int tempDegToShow = Math.round(this.width / pixelPerDegree);
		yawNumDegreesToShow = tempDegToShow - (tempDegToShow % 10);
		yawDegreesPerPixel = this.width / yawNumDegreesToShow;
		
		attHeightPx = this.height - yawHeightPx;
		attSizePxInfoText = Math.round(attHeightPx * ATT_FACTOR_INFOTEXT * hudDensity);
		attInfoText.setTextSize(attSizePxInfoText);
		tempMetrics = attInfoText.getFontMetricsInt();
		tempOffset = tempMetrics.top - tempMetrics.ascent; 
		tempClearance = Math.round(attSizePxInfoText * ATT_FACTOR_INFOTEXT_CLEARANCE);
		attPosPxInfoTextUpperTop = -attHeightPx / 2 + attSizePxInfoText + tempOffset + tempClearance;
		attPosPxInfoTextUpperBottom = -attHeightPx / 2 + 2 * attSizePxInfoText + tempOffset + 2 * tempClearance;
		attPosPxInfoTextLowerBottom = attHeightPx / 2 + tempOffset - tempClearance;
		attPosPxInfoTextLowerTop = attHeightPx / 2 - attSizePxInfoText + tempOffset - 2 * tempClearance;
		
		int scrollerMaxAvailHeight = attHeightPx - 4 * attSizePxInfoText - 6 * tempClearance;
		int scrollerMaxHeightPx = Math.round(attHeightPx * SCROLLER_MAX_HEIGHT_FACTOR * hudDensity);
		if (scrollerMaxAvailHeight < scrollerMaxHeightPx) 
			scrollerHeightPx = scrollerMaxAvailHeight;
		else 
			scrollerHeightPx = scrollerMaxHeightPx;		
		scrollerWidthPx = Math.round(this.width * SCROLLER_WIDTH_FACTOR);
		scrollerSizePxText = Math.round(attHeightPx * SCROLLER_FACTOR_TEXT * hudDensity);
		scrollerText.setTextSize(scrollerSizePxText);
		tempMetrics = scrollerText.getFontMetricsInt();
		scrollerSizePxTextOffset = tempMetrics.top - tempMetrics.ascent;
		scrollerSizePxArrowHeight = Math.round(scrollerSizePxText * SCROLLER_FACTOR_ARROW_HEIGTH);
		
		rollTopOffsetPx = Math.round(attHeightPx * ROLL_TOP_OFFSET_FACTOR * hudDensity);
		rollText.setTextSize(Math.round(attHeightPx * ROLL_FACTOR_TEXT * hudDensity));
		
		int tempSize = Math.round(attHeightPx * PITCH_FACTOR_TEXT * hudDensity);
		pitchText.setTextSize(tempSize);
		tempMetrics = pitchText.getFontMetricsInt();
		pitchTextCenterOffsetPx = Math.round(-tempSize / 2 - (tempMetrics.top - tempMetrics.ascent));
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

	public void setDrone(Drone	drone) {
		this.drone = drone;		
		this.drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		if (renderer != null)
			renderer.setDirty();
	}
}
