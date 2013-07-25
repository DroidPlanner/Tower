package com.droidplanner.widgets.HUD;

import android.graphics.Paint;

public class HudPlane {

	// in relation to averaged of width and height
	static final float HUD_FACTOR_CENTER_INDICATOR_SIZE = .0375f;

	public int hudCenterIndicatorRadius;
	public Paint plane = new Paint();

	public HudPlane() {
	}

	void setupPlane(HUDwidget huDwidget) {
		hudCenterIndicatorRadius = Math
				.round((huDwidget.width + huDwidget.height) / 2
						* HUD_FACTOR_CENTER_INDICATOR_SIZE);
	}
}