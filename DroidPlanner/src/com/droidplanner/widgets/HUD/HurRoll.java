package com.droidplanner.widgets.HUD;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

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

	void setupRoll(HUDwidget huDwidget) {
		rollTopOffsetPx = huDwidget.hudYaw.yawHeightPx;
		rollText.setTextSize(Math.round(huDwidget.data.attHeightPx * HurRoll.ROLL_FACTOR_TEXT));
		rollSizePxTics = Math.round(rollTopOffsetPx
				* HurRoll.ROLL_FACTOR_TIC_LENGTH);
		rollPosPxTextYOffset = Math.round(rollSizePxTics
				* HurRoll.ROLL_FACTOR_TEXT_Y_OFFSET);
	}
}