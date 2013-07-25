package com.droidplanner.widgets.HUD;

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

	public HudYaw() {
		yawText.setColor(Color.WHITE);
		yawText.setFakeBoldText(true);
		yawText.setTextAlign(Align.CENTER);
		yawText.setAntiAlias(true);

		yawNumbers.setColor(Color.WHITE);
		yawNumbers.setTextAlign(Align.CENTER);
		yawNumbers.setAntiAlias(true);
	}

	void setupYaw(HUDwidget huDwidget, HUDwidget hud) {
		int tempSize;
		int tempOffset;
		yawHeightPx = Math.round(hud.height * YAW_HEIGHT_FACTOR);
		yawSizePxTicsSmall = Math.round(yawHeightPx
				* YAW_FACTOR_TICS_SMALL);
		yawSizePxTicsTall = Math.round(yawHeightPx
				* YAW_FACTOR_TICS_TALL);
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
}