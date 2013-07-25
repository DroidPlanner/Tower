package com.droidplanner.widgets.HUD;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class HudInfo {

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

	public HudInfo() {
	}

	void setupAtt(HUD huDwidget) {
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

	void drawAttitudeInfoText(HUD huDwidget, Canvas canvas) {
		double battVolt = huDwidget.drone.battery.getBattVolt();
		double battCurrent = huDwidget.drone.battery.getBattCurrent();
		double battRemain = huDwidget.drone.battery.getBattRemain();
		double groundSpeed = huDwidget.drone.speed.getGroundSpeed();
		double airSpeed = huDwidget.drone.speed.getAirSpeed();
		int satCount = huDwidget.drone.GPS.getSatCount();
		int fixType = huDwidget.drone.GPS.getFixType();
		String modeName = huDwidget.drone.state.getMode().getName();
		int wpNumber = huDwidget.drone.mission.getWpno();
		double distToWp = huDwidget.drone.mission.getDisttowp();
		double gpsEPH = huDwidget.drone.GPS.getGpsEPH();
	
		if (HUD.hudDebug) {
			battVolt = HUD.hudDebugBattVolt;
			battCurrent = HUD.hudDebugBattCurrent;
			battRemain = HUD.hudDebugBattRemain;
			groundSpeed = HUD.hudDebugGroundSpeed;
			airSpeed = HUD.hudDebugAirSpeed;
			satCount = HUD.hudDebugSatCount;
			fixType = HUD.hudDebugFixType;
			modeName = HUD.hudDebugModeName;
			wpNumber = HUD.hudDebugWpNumber;
			distToWp = HUD.hudDebugDistToWp;
			gpsEPH = HUD.hudDebugGpsEPH;
		}
	
		// Left Top Text
		attInfoText.setTextAlign(Align.LEFT);
	
		if ((battVolt >= 0) || (battRemain >= 0))
			canvas.drawText(
					String.format("%2.1fV  %.0f%%", battVolt, battRemain),
					-huDwidget.width / 2 + attPosPxInfoTextXOffset,
					attPosPxInfoTextUpperTop, attInfoText);
		if (battCurrent >= 0)
			canvas.drawText(String.format("%2.1fA", battCurrent), -huDwidget.width / 2
					+ attPosPxInfoTextXOffset,
					attPosPxInfoTextUpperBottom, attInfoText);
	
		// Left Bottom Text
		canvas.drawText(String.format("AS %.1fms", airSpeed), -huDwidget.width / 2
				+ attPosPxInfoTextXOffset, attPosPxInfoTextLowerTop,
				attInfoText);
		canvas.drawText(String.format("GS %.1fms", groundSpeed), -huDwidget.width / 2
				+ attPosPxInfoTextXOffset,
				attPosPxInfoTextLowerBottom, attInfoText);
	
		// Right Top Text
		attInfoText.setTextAlign(Align.RIGHT);
	
		String gpsFix = "";
		if (satCount >= 0) {
			switch (fixType) {
			case 2:
				gpsFix = ("GPS2D(" + satCount + ")");
				break;
			case 3:
				gpsFix = ("GPS3D(" + satCount + ")");
				break;
			default:
				gpsFix = ("NoGPS(" + satCount + ")");
				break;
			}
		}
		canvas.drawText(gpsFix, huDwidget.width / 2 - attPosPxInfoTextXOffset,
				attPosPxInfoTextUpperTop, attInfoText);
		if (gpsEPH >= 0)
			canvas.drawText(String.format("hp%.1fm", gpsEPH), huDwidget.width / 2
					- attPosPxInfoTextXOffset,
					attPosPxInfoTextUpperBottom, attInfoText);
	
		// Right Bottom Text
		canvas.drawText(modeName, huDwidget.width / 2 - attPosPxInfoTextXOffset,
				attPosPxInfoTextLowerTop, attInfoText);
		if (wpNumber >= 0)
			canvas.drawText(String.format("%.0fm>WP#%d", distToWp, wpNumber),
					huDwidget.width / 2 - attPosPxInfoTextXOffset,
					attPosPxInfoTextLowerBottom, attInfoText);
	}
}