package com.droidplanner.widgets.HUD;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

public class HudScroller {
	// in relation to width (total HUD widget width)
	static final float SCROLLER_WIDTH_FACTOR = .15f;
	// in relation to attHeightPx
	static final float SCROLLER_MAX_HEIGHT_FACTOR = .66f;
	// in relation to attHeightPx
	static final float SCROLLER_FACTOR_TEXT = .048f;
	// in relation to scrollerSizePxText
	static final float SCROLLER_FACTOR_TEXT_Y_OFFSET = -.16f;
	// in relation to the resulting size of SCROLLER_FACTOR_TEXT
	static final float SCROLLER_FACTOR_ACTUAL_TEXT_MAGNIFICATION = 1.2f;
	// in relation to width
	static final float SCROLLER_FACTOR_TEXT_X_OFFSET = .037f;
	// in relation to width
	static final float SCROLLER_FACTOR_TIC_LENGTH = .025f;
	// in relation to scrollerSizePxText
	static final float SCROLLER_FACTOR_ARROW_HEIGTH = 1.4f;
	// in relation to attHeightPx
	static final float SCROLLER_FACTOR_TARGET_BAR_WIDTH = .015f;
	static final int SCROLLER_VSI_RANGE = 12;
	static final int SCROLLER_ALT_RANGE = 26;
	static final int SCROLLER_SPEED_RANGE = 26;

	public int scrollerHeightPx;
	public int scrollerWidthPx;
	public int scrollerSizePxTextYOffset;
	public int scrollerSizePxActualTextYOffset;
	public int scrollerSizePxTextXOffset;
	public int scrollerSizePxArrowHeight;
	public int scrollerSizePxTicLength;

	Paint scrollerBg = new Paint();
	Paint scrollerText = new Paint();
	Paint scrollerActualText = new Paint();
	Paint greenPen = new Paint();
	Paint blueVSI = new Paint();

	public HudScroller() {
		scrollerBg.setARGB(64, 255, 255, 255);// (255, 0, 0, 0);
		
		scrollerText.setColor(Color.WHITE);
		scrollerText.setAntiAlias(true);
		
		scrollerActualText.setColor(Color.WHITE);
		scrollerActualText.setAntiAlias(true);
		
		greenPen.setColor(Color.GREEN);
		greenPen.setStrokeWidth(6);
		greenPen.setStyle(Style.STROKE);
		
		blueVSI.setARGB(255, 0, 50, 250);
		blueVSI.setAntiAlias(true);
	}

	int setupScroller(HUD huDwidget, int tempAttTextClearance,
			int tempAttSizeText) {
		int tempSize;
		int scrollerMaxAvailHeight = huDwidget.data.attHeightPx - 4
				* tempAttSizeText - 6 * tempAttTextClearance;
		scrollerHeightPx = Math.round(huDwidget.data.attHeightPx
				* SCROLLER_MAX_HEIGHT_FACTOR);
		if (scrollerHeightPx > scrollerMaxAvailHeight) {
			scrollerHeightPx = scrollerMaxAvailHeight;
		} else {
			tempAttTextClearance = Math.round((huDwidget.data.attHeightPx
					- scrollerHeightPx - 4 * tempAttSizeText) / 6);
		}
		scrollerWidthPx = Math.round(huDwidget.width * SCROLLER_WIDTH_FACTOR);
		tempSize = Math
				.round(huDwidget.data.attHeightPx * SCROLLER_FACTOR_TEXT);
		scrollerText.setTextSize(tempSize);
		scrollerSizePxTextYOffset = Math.round(tempSize
				* SCROLLER_FACTOR_TEXT_Y_OFFSET);
		tempSize = Math.round(tempSize
				* SCROLLER_FACTOR_ACTUAL_TEXT_MAGNIFICATION);
		scrollerActualText.setTextSize(tempSize);
		scrollerSizePxActualTextYOffset = Math.round(tempSize
				* SCROLLER_FACTOR_TEXT_Y_OFFSET);
		scrollerSizePxArrowHeight = Math.round(tempSize
				* SCROLLER_FACTOR_ARROW_HEIGTH);
		scrollerSizePxTextXOffset = Math.round(huDwidget.width
				* SCROLLER_FACTOR_TEXT_X_OFFSET);
		scrollerSizePxTicLength = Math.round(huDwidget.width
				* SCROLLER_FACTOR_TIC_LENGTH);

		greenPen.setStrokeWidth(Math.round(huDwidget.data.attHeightPx
				* HudScroller.SCROLLER_FACTOR_TARGET_BAR_WIDTH));
		return tempAttTextClearance;
	}

	void drawLeftScroller(HUD huDwidget, Canvas canvas) {
		final float textHalfSize = scrollerText.getTextSize() / 2;
		scrollerText.setTextAlign(Paint.Align.RIGHT);
		scrollerActualText.setTextAlign(Paint.Align.RIGHT);

		double groundSpeed = huDwidget.drone.speed.getGroundSpeed();
		double airSpeed = huDwidget.drone.speed.getAirSpeed();
		double targetSpeed = huDwidget.drone.speed.getTargetSpeed();
		if (HUD.hudDebug) {
			groundSpeed = HUD.hudDebugGroundSpeed;
			airSpeed = HUD.hudDebugAirSpeed;
			targetSpeed = HUD.hudDebugTargetSpeed;
		}

		double speed = airSpeed;
		if (speed == 0)
			speed = groundSpeed;

		// Outside box
		RectF scroller = new RectF(-huDwidget.width / 2, -scrollerHeightPx / 2,
				-huDwidget.width / 2 + scrollerWidthPx, scrollerHeightPx / 2);

		// Draw Scroll
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, huDwidget.whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height()
				/ (float) HudScroller.SCROLLER_SPEED_RANGE;
		int start = ((int) speed - HudScroller.SCROLLER_SPEED_RANGE / 2);

		if (start > targetSpeed) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((speed + HudScroller.SCROLLER_SPEED_RANGE / 2) < targetSpeed) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}

		float targetSpdPos = Float.MIN_VALUE;
		for (int a = start; a <= (speed + HudScroller.SCROLLER_SPEED_RANGE / 2); a += 1) {
			float lineHeight = scroller.centerY() - space * (a - (int) speed);

			if (a == ((int) targetSpeed) && targetSpeed != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right,
						lineHeight, greenPen);
				targetSpdPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.right, lineHeight, scroller.right
						- scrollerSizePxTicLength, lineHeight,
						huDwidget.whiteThickTics);
				canvas.drawText(Integer.toString(a), scroller.right
						- scrollerSizePxTextXOffset, lineHeight + textHalfSize
						+ scrollerSizePxTextYOffset, scrollerText);
			}
		}

		// Arrow with current speed
		String actualText = Integer.toString((int) speed);
		int borderWidth = Math.round(huDwidget.whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.left - borderWidth,
				-scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4
				- borderWidth, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right - borderWidth, 0);
		arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4
				- borderWidth, scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left - borderWidth, scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, huDwidget.blackSolid);
		if ((targetSpdPos != Float.MIN_VALUE)
				&& (targetSpdPos > -scrollerSizePxArrowHeight / 2)
				&& (targetSpdPos < scrollerSizePxArrowHeight / 2)) {
			Rect actualTextRec = new Rect();
			scrollerActualText.getTextBounds(actualText, 0,
					actualText.length(), actualTextRec);
			canvas.drawLine(scroller.left, targetSpdPos, scroller.right
					- actualTextRec.width() - scrollerSizePxTextXOffset
					- textHalfSize, targetSpdPos, greenPen);
		}
		canvas.drawPath(arrow, huDwidget.hudPlane.plane);
		canvas.drawText(actualText, scroller.right - scrollerSizePxTextXOffset,
				scrollerActualText.getTextSize() / 2
						+ scrollerSizePxActualTextYOffset, scrollerActualText);
		// Reset clipping of Scroller
		canvas.clipRect(-huDwidget.width / 2, -huDwidget.height / 2,
				huDwidget.width / 2, huDwidget.height / 2, Region.Op.REPLACE);
	}

	void drawRightScroller(HUD huDwidget, Canvas canvas) {
		final float textHalfSize = scrollerText.getTextSize() / 2;
		scrollerText.setTextAlign(Paint.Align.LEFT);
		scrollerActualText.setTextAlign(Paint.Align.LEFT);

		double altitude = huDwidget.drone.altitude.getAltitude();
		double targetAltitude = huDwidget.drone.altitude.getTargetAltitude();
		double verticalSpeed = huDwidget.drone.speed.getVerticalSpeed();

		if (HUD.hudDebug) {
			altitude = HUD.hudDebugAltitude;
			targetAltitude = HUD.hudDebugTargetAltitude;
			verticalSpeed = HUD.hudDebugVerticalSpeed;
		}

		// Outside box
		RectF scroller = new RectF(huDwidget.width / 2 - scrollerWidthPx,
				-scrollerHeightPx / 2, huDwidget.width / 2,
				scrollerHeightPx / 2);

		// Draw Vertical speed indicator
		final float vsi_width = scroller.width() / 4;
		float linespace = scroller.height() / HudScroller.SCROLLER_VSI_RANGE;
		Path vsiBox = new Path();
		vsiBox.moveTo(scroller.left, scroller.top); // draw outside box
		vsiBox.lineTo(scroller.left - vsi_width, scroller.top + vsi_width);
		vsiBox.lineTo(scroller.left - vsi_width, scroller.bottom - vsi_width);
		vsiBox.lineTo(scroller.left, scroller.bottom);
		Path vsiFill = new Path();
		float vsiIndicatorEnd = scroller.centerY() - ((float) verticalSpeed)
				* linespace;
		vsiFill.moveTo(scroller.left, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width, scroller.centerY());
		vsiFill.lineTo(scroller.left - vsi_width, vsiIndicatorEnd);
		vsiFill.lineTo(scroller.left, vsiIndicatorEnd);
		vsiFill.lineTo(scroller.left, scroller.centerY());
		canvas.drawPath(vsiBox, scrollerBg);
		canvas.drawPath(vsiFill, blueVSI);
		canvas.drawLine(scroller.left - vsi_width, vsiIndicatorEnd,
				scroller.left, vsiIndicatorEnd, huDwidget.whiteThinTics);
		canvas.drawPath(vsiBox, huDwidget.whiteBorder);

		for (int a = 1; a < HudScroller.SCROLLER_VSI_RANGE; a++) { // draw ticks
			float lineHeight = scroller.top + linespace * a;
			canvas.drawLine(scroller.left - vsi_width, lineHeight,
					scroller.left - vsi_width + vsi_width / 3, lineHeight,
					huDwidget.whiteThickTics);
		}

		// Draw Altitude Scroller
		canvas.drawRect(scroller, scrollerBg);
		canvas.drawRect(scroller, huDwidget.whiteBorder);
		// Clip to Scroller
		canvas.clipRect(scroller, Region.Op.REPLACE);

		float space = scroller.height()
				/ (float) HudScroller.SCROLLER_ALT_RANGE;
		int start = ((int) altitude - HudScroller.SCROLLER_ALT_RANGE / 2);

		if (start > targetAltitude) {
			canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
					scroller.bottom, greenPen);
		} else if ((altitude + HudScroller.SCROLLER_SPEED_RANGE / 2) < targetAltitude) {
			canvas.drawLine(scroller.left, scroller.top, scroller.right,
					scroller.top, greenPen);
		}

		float targetAltPos = Float.MIN_VALUE;
		for (int a = start; a <= (altitude + HudScroller.SCROLLER_ALT_RANGE / 2); a += 1) { // go
			// trough
			// 1m
			// steps

			float lineHeight = scroller.centerY() - space
					* (a - (int) altitude);

			if (a == ((int) targetAltitude) && targetAltitude != 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.right,
						lineHeight, greenPen);
				targetAltPos = lineHeight;
			}
			if (a % 5 == 0) {
				canvas.drawLine(scroller.left, lineHeight, scroller.left
						+ scrollerSizePxTicLength, lineHeight,
						huDwidget.whiteThickTics);
				canvas.drawText(Integer.toString(a), scroller.left
						+ scrollerSizePxTextXOffset, lineHeight + textHalfSize
						+ scrollerSizePxTextYOffset, scrollerText);
			}
		}

		// Arrow with current altitude
		String actualText = Integer.toString((int) altitude);
		int borderWidth = Math.round(huDwidget.whiteBorder.getStrokeWidth());
		Path arrow = new Path();
		arrow.moveTo(scroller.right, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4
				+ borderWidth, -scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.left + borderWidth, 0);
		arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4
				+ borderWidth, scrollerSizePxArrowHeight / 2);
		arrow.lineTo(scroller.right, scrollerSizePxArrowHeight / 2);
		canvas.drawPath(arrow, huDwidget.blackSolid);
		if ((targetAltPos != Float.MIN_VALUE)
				&& (targetAltPos > -scrollerSizePxArrowHeight / 2)
				&& (targetAltPos < scrollerSizePxArrowHeight / 2)) {
			Rect actualTextRec = new Rect();
			scrollerActualText.getTextBounds(actualText, 0,
					actualText.length(), actualTextRec);
			canvas.drawLine(scroller.right, targetAltPos, scroller.left
					+ actualTextRec.width() + scrollerSizePxTextXOffset
					+ textHalfSize, targetAltPos, greenPen);
		}
		canvas.drawPath(arrow, huDwidget.hudPlane.plane);
		canvas.drawText(actualText, scroller.left + scrollerSizePxTextXOffset,
				scrollerActualText.getTextSize() / 2
						+ scrollerSizePxActualTextYOffset, scrollerActualText);
		// Reset clipping of Scroller
		canvas.clipRect(-huDwidget.width / 2, -huDwidget.height / 2,
				huDwidget.width / 2, huDwidget.height / 2, Region.Op.REPLACE);
		// Draw VSI center indicator
		canvas.drawLine(scroller.left + borderWidth, 0, scroller.left
				- vsi_width - borderWidth, 0, huDwidget.hudPlane.plane);
	}
}