package com.droidplanner.widgets.HUD;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class HudCommonPaints {
	// in relation to averaged of width and height
	static final float HUD_FACTOR_BORDER_WIDTH = .0075f;
	// in relation to averaged of width and height
	static final float HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH = .005f;
	// in relation to averaged of width and height
	static final float HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH = .0025f;

	
	public Paint whiteBorder = new Paint();
	public Paint whiteThickTics = new Paint();
	public Paint whiteThinTics = new Paint();
	public Paint blackSolid = new Paint();

	public HudCommonPaints() {

		whiteBorder.setColor(Color.WHITE);
		whiteBorder.setStyle(Style.STROKE);
		whiteBorder.setStrokeWidth(3);
		whiteBorder.setAntiAlias(true);

		whiteThinTics.setColor(Color.WHITE);
		whiteThinTics.setStyle(Style.FILL);
		whiteThinTics.setStrokeWidth(1);
		whiteThinTics.setAntiAlias(true);

		whiteThickTics.setColor(Color.WHITE);
		whiteThickTics.setStyle(Style.FILL);
		whiteThickTics.setStrokeWidth(2);
		whiteThickTics.setAntiAlias(true);

		blackSolid.setColor(Color.BLACK);
		blackSolid.setAntiAlias(true);
	}

	void setupCommonPaints(HUD hud) {
		float hudScaleThickTicStrokeWidth;
		float hudScaleThinTicStrokeWidth;
		float hudBorderWidth;
		hudScaleThickTicStrokeWidth = (hud.width + hud.height) / 2
				* HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH;
		if (hudScaleThickTicStrokeWidth < 1)
			hudScaleThickTicStrokeWidth = 1;
		whiteThickTics.setStrokeWidth(hudScaleThickTicStrokeWidth);
	
		hudScaleThinTicStrokeWidth = (hud.width + hud.height) / 2
				* HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH;
		if (hudScaleThinTicStrokeWidth < 1)
			hudScaleThinTicStrokeWidth = 1;
		whiteThinTics.setStrokeWidth(hudScaleThinTicStrokeWidth);
	
		hudBorderWidth = (hud.width + hud.height) / 2
				* HUD_FACTOR_BORDER_WIDTH;
		if (hudBorderWidth < 1)
			hudBorderWidth = 1;
		whiteBorder.setStrokeWidth(hudBorderWidth);
	}
}