package com.droidplanner.glass.utils.hud;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;

public class HudPitch {
	// in relation to attHeightPx
	static final float PITCH_FACTOR_TEXT = .038f;
	// in relation to the resulting size of PITCH_FACTOR_TEXT
	static final float PITCH_FACTOR_TEXT_Y_OFFSET = -.16f;
	// in relation to attHeightPx
	static final float PITCH_FACTOR_SCALE_Y_SPACE = 0.02f;
	// in relation to width
	static final float PITCH_FACTOR_SCALE_WIDHT_WIDE = 0.25f;
	// in relation to width
	static final float PITCH_FACTOR_SCALE_WIDHT_NARROW = 0.1f;
	// in relation to width
	static final float PITCH_FACTOR_SCALE_TEXT_X_OFFSET = 0.025f;

	public int pitchTextCenterOffsetPx;
	public int pitchPixPerDegree;
	public int pitchScaleWideHalfWidth;
	public int pitchScaleNarrowHalfWidth;
	public int pitchScaleTextXOffset;

	Paint ground = new Paint();
	Paint sky = new Paint();
	Paint pitchText = new Paint();

	public HudPitch() {
		ground.setARGB(220, 148, 193, 31);
		sky.setARGB(220, 0, 113, 188);

		pitchText.setColor(Color.WHITE);
		pitchText.setAntiAlias(true);
		pitchText.setTextAlign(Align.RIGHT);
	}

	void setupPitch(HUD huDwidget) {
		int tempSize;
		tempSize = Math
				.round(huDwidget.hudInfo.attHeightPx * PITCH_FACTOR_TEXT);
		pitchText.setTextSize(tempSize);
		pitchTextCenterOffsetPx = Math.round(-tempSize / 2 - tempSize
				* PITCH_FACTOR_TEXT_Y_OFFSET);
		pitchScaleWideHalfWidth = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_WIDHT_WIDE / 2);
		pitchScaleNarrowHalfWidth = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_WIDHT_NARROW / 2);
		pitchScaleTextXOffset = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_TEXT_X_OFFSET);
		pitchPixPerDegree = Math.round(huDwidget.hudInfo.attHeightPx
				* PITCH_FACTOR_SCALE_Y_SPACE);
	}

	void drawPitch(HUD huDwidget, Canvas canvas) {
		double pitch = huDwidget.drone.orientation.getPitch();
		double roll = huDwidget.drone.orientation.getRoll();

		if (HudDebugData.hudDebug) {
			pitch = HudDebugData.hudDebugPitch;
			roll = HudDebugData.hudDebugRoll;
		}

		int pitchOffsetPx = (int) (pitch * pitchPixPerDegree);
		int rollTriangleBottom = -huDwidget.hudInfo.attHeightPx / 2
				+ huDwidget.hudRoll.rollTopOffsetPx / 2
				+ huDwidget.hudRoll.rollTopOffsetPx;

		canvas.rotate(-(int) roll);

		// Draw the background
		canvas.drawRect(-huDwidget.width, pitchOffsetPx, huDwidget.width,
				2 * huDwidget.height, ground);
		canvas.drawRect(-huDwidget.width, -2 * huDwidget.height,
				huDwidget.width, pitchOffsetPx, sky);
		canvas.drawLine(-huDwidget.width, pitchOffsetPx, huDwidget.width,
				pitchOffsetPx, huDwidget.commonPaints.whiteThinTics);

		// Draw roll triangle
		Path arrow = new Path();
		int tempOffset = Math.round(huDwidget.hudPlane.plane.getStrokeWidth()
				+ huDwidget.commonPaints.whiteBorder.getStrokeWidth() / 2);
		arrow.moveTo(0, -huDwidget.hudInfo.attHeightPx / 2
				+ huDwidget.hudRoll.rollTopOffsetPx + tempOffset);
		arrow.lineTo(0 - huDwidget.hudRoll.rollTopOffsetPx / 3,
				rollTriangleBottom + tempOffset);
		arrow.lineTo(0 + huDwidget.hudRoll.rollTopOffsetPx / 3,
				rollTriangleBottom + tempOffset);
		arrow.close();
		canvas.drawPath(arrow, huDwidget.hudPlane.plane);

		// Draw gauge
		int yPos;
		for (int i = -180; i <= 180; i += 5) {
			yPos = Math.round(-i * pitchPixPerDegree + pitchOffsetPx);
			if ((yPos < -rollTriangleBottom) && (yPos > rollTriangleBottom)
					&& (yPos != pitchOffsetPx)) {
				if (i % 2 == 0) {
					canvas.drawLine(-pitchScaleWideHalfWidth, yPos,
							pitchScaleWideHalfWidth, yPos,
							huDwidget.commonPaints.whiteThinTics);
					canvas.drawText(i + "", -pitchScaleWideHalfWidth
							- pitchScaleTextXOffset, yPos
							- pitchTextCenterOffsetPx, pitchText);
				} else
					canvas.drawLine(-pitchScaleNarrowHalfWidth, yPos,
							pitchScaleNarrowHalfWidth, yPos,
							huDwidget.commonPaints.whiteThinTics);
			}
		}

		canvas.rotate((int) roll);
	}
}