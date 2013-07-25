package com.droidplanner.widgets.HUD;

import android.graphics.Paint;

public class HudAtt {

	// in relation to attHeightPx
	static final float ATT_FACTOR_INFOTEXT = .048f;
	// in relation to the resulting size of ATT_FACTOR_INFOTEXT
	static final float ATT_FACTOR_INFOTEXT_Y_OFFSET = -.1f;
	// in relation to width
	static final float ATT_FACTOR_INFOTEXT_X_OFFSET = .013f;
	// in relation to attSizePxInfoText
	static final float ATT_FACTOR_INFOTEXT_CLEARANCE = .1f;
	
	public int attHeightPx;
	public int attPosPxInfoTextUpperTop;
	public int attPosPxInfoTextUpperBottom;
	public int attPosPxInfoTextLowerTop;
	public int attPosPxInfoTextLowerBottom;
	public int attPosPxInfoTextXOffset;
	Paint attInfoText = new Paint();

	public HudAtt() {
	}

	void setupAtt(HUDwidget huDwidget) {
		int tempAttTextClearance;
		int tempOffset;
		int tempAttSizeText;
		attHeightPx = huDwidget.height - huDwidget.hudYaw.yawHeightPx;
		tempAttSizeText = Math.round(attHeightPx * ATT_FACTOR_INFOTEXT);
		attInfoText.setTextSize(tempAttSizeText);
		tempOffset = Math.round(tempAttSizeText * ATT_FACTOR_INFOTEXT_Y_OFFSET);
		tempAttTextClearance = Math.round(tempAttSizeText
				* ATT_FACTOR_INFOTEXT_CLEARANCE);
		attPosPxInfoTextXOffset = Math.round(huDwidget.width
				* ATT_FACTOR_INFOTEXT_X_OFFSET);
	
		tempAttTextClearance = huDwidget.hudScroller.setupScroller(huDwidget,
				tempAttTextClearance, tempAttSizeText);
	
		attPosPxInfoTextUpperTop = -attHeightPx / 2 + tempAttSizeText
				+ tempOffset + tempAttTextClearance;
		attPosPxInfoTextUpperBottom = -attHeightPx / 2 + 2
				* tempAttSizeText + tempOffset + 2 * tempAttTextClearance;
		attPosPxInfoTextLowerBottom = attHeightPx / 2 + tempOffset
				- tempAttTextClearance;
		attPosPxInfoTextLowerTop = attHeightPx / 2 - tempAttSizeText
				+ tempOffset - 2 * tempAttTextClearance;
	}
}