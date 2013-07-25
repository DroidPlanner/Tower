package com.droidplanner.widgets.HUD;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class HudYaw {
	// in relation to height (total HUD widget height)
	static final float YAW_HEIGHT_FACTOR = .075f;
	// in relation to yawHeightPx
	static final float YAW_FACTOR_TEXT = .75f;
	// in relation to yawHeightPx
	static final float YAW_FACTOR_TEXT_NUMBERS = .50f;
	// in relation to yawSizePxText
	static final float YAW_FACTOR_TEXT_Y_OFFSET = -.16f;
	// in relation to yawHeightPx
	static final float YAW_FACTOR_TICS_SMALL = .20f;
	// in relation to yawHeightPx
	static final float YAW_FACTOR_TICS_TALL = .35f;
	// in relation to yawHeightPx
	static final float YAW_FACTOR_CENTERLINE_OVERRUN = .2f;
	static final int YAW_DEGREES_TO_SHOW = 90;

	public int yawHeightPx;
	public int yawYPosPxText;
	public int yawYPosPxTextNumbers;
	public double yawDegreesPerPixel;
	public int yawSizePxTicsSmall;
	public int yawSizePxTicsTall;
	public int yawSizePxCenterLineOverRun;
	Paint yawText = new Paint();
	Paint yawNumbers = new Paint();
	// Paints
	Paint yawBg = new Paint();

	public HudYaw() {
		yawText.setColor(Color.WHITE);
		yawText.setFakeBoldText(true);
		yawText.setTextAlign(Align.CENTER);
		yawText.setAntiAlias(true);

		yawNumbers.setColor(Color.WHITE);
		yawNumbers.setTextAlign(Align.CENTER);
		yawNumbers.setAntiAlias(true);
		
		yawBg.setARGB(255, 0, 0, 0);// (64, 255, 255, 255);
	}

	void setupYaw(HUD huDwidget, HUD hud) {
		int tempSize;
		int tempOffset;
		yawHeightPx = Math.round(hud.height * YAW_HEIGHT_FACTOR);
		yawSizePxTicsSmall = Math.round(yawHeightPx * YAW_FACTOR_TICS_SMALL);
		yawSizePxTicsTall = Math.round(yawHeightPx * YAW_FACTOR_TICS_TALL);
		tempSize = Math.round(yawHeightPx * YAW_FACTOR_TEXT);
		yawText.setTextSize(tempSize);
		tempOffset = Math.round(tempSize * YAW_FACTOR_TEXT_Y_OFFSET);
		yawYPosPxText = Math.round(yawSizePxTicsSmall
				+ (yawHeightPx - yawSizePxTicsSmall) / 2 - tempSize / 2
				- tempOffset);
		tempSize = Math.round(yawHeightPx * YAW_FACTOR_TEXT_NUMBERS);
		yawNumbers.setTextSize(tempSize);
		tempOffset = Math.round(tempSize * YAW_FACTOR_TEXT_Y_OFFSET);
		yawYPosPxTextNumbers = Math.round(yawSizePxTicsSmall
				+ (yawHeightPx - yawSizePxTicsSmall) / 2 - tempSize / 2
				- tempOffset);
		yawSizePxCenterLineOverRun = Math.round(yawHeightPx
				* YAW_FACTOR_CENTERLINE_OVERRUN);
		yawDegreesPerPixel = hud.width / YAW_DEGREES_TO_SHOW;
	}

	void drawYaw(HUD huDwidget, Canvas canvas) {
		int yawBottom = -huDwidget.data.attHeightPx / 2;
		canvas.drawRect(-huDwidget.width / 2, yawBottom - yawHeightPx,
				huDwidget.width / 2, yawBottom, yawBg);
		canvas.drawLine(-huDwidget.width / 2, yawBottom, huDwidget.width / 2,
				yawBottom, huDwidget.whiteBorder);

		double yaw = huDwidget.drone.orientation.getYaw();
		if (HUD.hudDebug)
			yaw = HUD.hudDebugYaw;

		double centerDegrees = yaw;

		double mod = yaw % 5;
		for (double angle = (centerDegrees - mod) - HudYaw.YAW_DEGREES_TO_SHOW
				/ 2.0; angle <= (centerDegrees - mod)
				+ HudYaw.YAW_DEGREES_TO_SHOW / 2.0; angle += 5) {

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
				canvas.drawLine(distanceToCenter, yawBottom
						- yawSizePxTicsSmall, distanceToCenter, yawBottom,
						huDwidget.whiteThinTics);
				canvas.drawText(compass[index], distanceToCenter, yawBottom
						- yawYPosPxText, yawText);
			} else if (workAngle % 15 == 0) {
				canvas.drawLine(distanceToCenter,
						yawBottom - yawSizePxTicsTall, distanceToCenter,
						yawBottom, huDwidget.whiteThinTics);
				canvas.drawText((int) (workAngle) + "", distanceToCenter,
						yawBottom - yawYPosPxTextNumbers, yawNumbers);
			} else {
				canvas.drawLine(distanceToCenter, yawBottom
						- yawSizePxTicsSmall, distanceToCenter, yawBottom,
						huDwidget.whiteThinTics);
			}
		}

		// Draw the center line
		canvas.drawLine(0, yawBottom - yawHeightPx, 0, yawBottom
				+ yawSizePxCenterLineOverRun, huDwidget.hudPlane.plane);
	}
}