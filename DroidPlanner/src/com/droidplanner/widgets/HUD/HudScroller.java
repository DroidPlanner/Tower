package com.droidplanner.widgets.HUD;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

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

	public HudScroller() {
		scrollerBg.setARGB(64, 255, 255, 255);// (255, 0, 0, 0);
		scrollerText.setColor(Color.WHITE);
		scrollerText.setAntiAlias(true);
		scrollerActualText.setColor(Color.WHITE);
		scrollerActualText.setAntiAlias(true);
		

		greenPen.setColor(Color.GREEN);
		greenPen.setStrokeWidth(6);
		greenPen.setStyle(Style.STROKE);
	}

	int setupScroller(HUDwidget huDwidget, int tempAttTextClearance,
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
}