package com.droidplanner.widgets.HUD;

import android.graphics.Paint;

public class HudFailsafe {

	// in relation to width
	static final float FAILSAFE_FACTOR_TEXT = .093f;
	// in relation to the resulting size of FAILSAFE_FACTOR_TEXT
	static final float FAILSAFE_FACTOR_BOX_PADDING = .27f;
	
	public Paint FailsafeText = new Paint();

	public HudFailsafe() {
		FailsafeText.setTextSize(37);
		FailsafeText.setAntiAlias(true);
	}

	void setupFailsafe(HUDwidget huDwidget) {
		int tempSize;
		tempSize = Math.round(huDwidget.width * FAILSAFE_FACTOR_TEXT);
		FailsafeText.setTextSize(tempSize);
		huDwidget.failsafeSizePxBoxPadding = Math.round(tempSize
				* FAILSAFE_FACTOR_BOX_PADDING);
	}
}