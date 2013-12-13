package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.helpers.math.MathUtil;

public class Radio extends DroneVariable {
	private static final int MAX_FADE_MARGIN = 100;
	private static final int MIN_FADE_MARGIN = 5;
	
	private int rxerrors = -1;
	private int fixed = -1;
	private int rssi = -1;
	private int remrssi = -1;
	private int txbuf = -1;
	private int noise = -1;
	private int remnoise = -1;

	public Radio(Drone myDrone) {
		super(myDrone);
	}

	public int getRxErrors() {
		return rxerrors;
	}

	public int getFixed() {
		return fixed;
	}

	public int getRssi() {
		return rssi;
	}

	public int getRemRssi() {
		return remrssi;
	}

	public int getTxBuf() {
		return txbuf;
	}

	public int getNoise() {
		return noise;
	}

	public int getRemNoise() {
		return remnoise;
	}

	public int getFadeMargin() {
		return rssi-noise;
	}

	public int getRemFadeMargin() {
		return remrssi-remnoise;
	}
	
	/**
	 * Signal Strength in percentage
	 * @return percentage
	 */
	public int getSignalStrength() {
		return (int) (MathUtil.Normalize(Math.min(getFadeMargin(),getRemFadeMargin()),MIN_FADE_MARGIN,MAX_FADE_MARGIN)*100);
	}
	
	public void setRadioState(short rxerrors, short fixed, byte rssi,
			byte remrssi, byte txbuf, byte noise, byte remnoise) {
		if (this.rxerrors != rxerrors | this.fixed != fixed | this.rssi != rssi
				| this.remrssi != remrssi | this.txbuf != txbuf
				| this.noise != noise | this.remnoise != remnoise) {

			this.rxerrors = rxerrors & 0xFFFF;
			this.fixed = fixed & 0xFFFF;
			this.rssi = rssi & 0xFF;
			this.remrssi = remrssi & 0xFF;
			this.txbuf = txbuf & 0xFF;
			this.noise = noise & 0xFF;
			this.remnoise = remnoise & 0xFF;
			myDrone.notifyInfoChange();
		}

	}

}
