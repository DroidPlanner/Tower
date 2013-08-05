package com.droidplanner.widgets.HUD;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.MAVLink.Messages.ApmModes;

public class HudFailsafe {

	// in relation to width
	static final float FAILSAFE_FACTOR_TEXT = .093f;
	// in relation to the resulting size of FAILSAFE_FACTOR_TEXT
	static final float FAILSAFE_FACTOR_BOX_PADDING = .27f;

	public Paint FailsafeText = new Paint();

	int failsafeSizePxBoxPadding;
	int armedCounter = 0;

	public HudFailsafe() {
		FailsafeText.setTextSize(37);
		FailsafeText.setAntiAlias(true);
	}

	void setupFailsafe(HUD huDwidget) {
		int tempSize;
		tempSize = Math.round(huDwidget.width * FAILSAFE_FACTOR_TEXT);
		FailsafeText.setTextSize(tempSize);
		failsafeSizePxBoxPadding = Math.round(tempSize
				* FAILSAFE_FACTOR_BOX_PADDING);
	}

	void drawFailsafe(HUD huDwidget, Canvas canvas) {
		int droneType = huDwidget.drone.type.getType();
		boolean isArmed = huDwidget.drone.state.isArmed();

		if (HudDebugData.hudDebug) {
			droneType = HudDebugData.hudDebugDroneType;
			isArmed = HudDebugData.hudDebugDroneArmed;
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
							failsafeSizePxBoxPadding,
							huDwidget.commonPaints.blackSolid);
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
						failsafeSizePxBoxPadding,
						huDwidget.commonPaints.blackSolid);
				canvas.drawText(text, textRec.left - 3, textRec.bottom - 1,
						FailsafeText);
				armedCounter = 0;
			}
		}
	}
}