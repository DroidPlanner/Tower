package com.droidplanner.widgets.HUD;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

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

	void setupPitch(HUDwidget huDwidget) {
		int tempSize;
		tempSize = Math.round(huDwidget.data.attHeightPx * PITCH_FACTOR_TEXT);
		pitchText.setTextSize(tempSize);
		pitchTextCenterOffsetPx = Math.round(-tempSize / 2 - tempSize
				* PITCH_FACTOR_TEXT_Y_OFFSET);
		pitchScaleWideHalfWidth = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_WIDHT_WIDE / 2);
		pitchScaleNarrowHalfWidth = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_WIDHT_NARROW / 2);
		pitchScaleTextXOffset = Math.round(huDwidget.width
				* PITCH_FACTOR_SCALE_TEXT_X_OFFSET);
		pitchPixPerDegree = Math.round(huDwidget.data.attHeightPx
				* PITCH_FACTOR_SCALE_Y_SPACE);
	}
}