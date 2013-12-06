package com.droidplanner.glass.utils.hud;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;

public class HurRoll {

	// in relation to attHeightPx
	static final float ROLL_FACTOR_TEXT = .038f;
	// in relation to rollTopOffsetPx
	static final float ROLL_FACTOR_TIC_LENGTH = .25f;
	// in relation to rollSizePxTics
	static final float ROLL_FACTOR_TEXT_Y_OFFSET = .8f;

	public int rollTopOffsetPx;
	public int rollSizePxTics;
	public int rollPosPxTextYOffset;
	Paint rollText = new Paint();

	public HurRoll() {
		rollText.setColor(Color.WHITE);
		rollText.setAntiAlias(true);
		rollText.setTextAlign(Align.CENTER);
	}

	void setupRoll(HUD huDwidget) {
		rollTopOffsetPx = huDwidget.hudYaw.yawHeightPx;
		rollText.setTextSize(Math.round(huDwidget.hudInfo.attHeightPx
				* HurRoll.ROLL_FACTOR_TEXT));
		rollSizePxTics = Math.round(rollTopOffsetPx
				* HurRoll.ROLL_FACTOR_TIC_LENGTH);
		rollPosPxTextYOffset = Math.round(rollSizePxTics
				* HurRoll.ROLL_FACTOR_TEXT_Y_OFFSET);
	}

	void drawRoll(HUD huDwidget, Canvas canvas) {
		int r = Math.round(huDwidget.hudInfo.attHeightPx / 2 - rollTopOffsetPx);
		RectF rec = new RectF(-r, -r, r, r);

		// Draw the arc
		canvas.drawArc(rec, 225, 90, false, huDwidget.commonPaints.whiteBorder);

		// Draw center triangle
		Path arrow = new Path();
		int tempOffset = Math
				.round(huDwidget.hudPlane.plane.getStrokeWidth() / 2);
		arrow.moveTo(0, -huDwidget.hudInfo.attHeightPx / 2 + rollTopOffsetPx
				- tempOffset);
		arrow.lineTo(0 - rollTopOffsetPx / 3, -huDwidget.hudInfo.attHeightPx
				/ 2 + rollTopOffsetPx / 2 - tempOffset);
		arrow.lineTo(0 + rollTopOffsetPx / 3, -huDwidget.hudInfo.attHeightPx
				/ 2 + rollTopOffsetPx / 2 - tempOffset);
		arrow.close();
		canvas.drawPath(arrow, huDwidget.hudPlane.plane);

		// draw the ticks
		// The center of the circle is at: 0, 0
		for (int i = -45; i <= 45; i += 15) {
			if (i != 0) {
				// Draw ticks
				float dx = (float) Math.sin(i * Math.PI / 180) * r;
				float dy = (float) Math.cos(i * Math.PI / 180) * r;
				float ex = (float) Math.sin(i * Math.PI / 180)
						* (r + rollSizePxTics);
				float ey = (float) Math.cos(i * Math.PI / 180)
						* (r + rollSizePxTics);
				canvas.drawLine(dx, -dy, ex, -ey,
						huDwidget.commonPaints.whiteThickTics);
				// Draw the labels
				dx = (float) Math.sin(i * Math.PI / 180)
						* (r + rollSizePxTics + rollPosPxTextYOffset);
				dy = (float) Math.cos(i * Math.PI / 180)
						* (r + rollSizePxTics + rollPosPxTextYOffset);
				canvas.drawText(Math.abs(i) + "", dx, -dy, rollText);
			}
		}

		// current roll angle will be drawn by drawPitch()
	}
}