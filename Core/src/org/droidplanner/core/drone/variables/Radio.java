package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.math.MathUtil;

public class Radio extends DroneVariable {
	private static final int MAX_FADE_MARGIN = 50;
	private static final int MIN_FADE_MARGIN = 6;

	private int rxerrors = -1;
	private int fixed = -1;
	private int txbuf = -1;
	private double rssi = -1;
	private double remrssi = -1;
	private double noise = -1;
	private double remnoise = -1;

	public Radio(Drone myDrone) {
		super(myDrone);
	}

	public int getRxErrors() {
		return rxerrors;
	}

	public int getFixed() {
		return fixed;
	}

	public double getRssi() {
		return rssi;
	}

	public double getRemRssi() {
		return remrssi;
	}

	public int getTxBuf() {
		return txbuf;
	}

	public double getNoise() {
		return noise;
	}

	public double getRemNoise() {
		return remnoise;
	}

	public double getFadeMargin() {
		return rssi - noise;
	}

	public double getRemFadeMargin() {
		return remrssi - remnoise;
	}

	/**
	 * Signal Strength in percentage
	 * 
	 * @return percentage
	 */
	public int getSignalStrength() {
		return (int) (MathUtil.Normalize(
				Math.min(getFadeMargin(), getRemFadeMargin()), MIN_FADE_MARGIN,
				MAX_FADE_MARGIN) * 100);
	}

	public void setRadioState(short rxerrors, short fixed, byte rssi,
			byte remrssi, byte txbuf, byte noise, byte remnoise) {
		if (this.rxerrors != rxerrors | this.fixed != fixed | this.rssi != rssi
				| this.remrssi != remrssi | this.txbuf != txbuf
				| this.noise != noise | this.remnoise != remnoise) {

			this.rxerrors = rxerrors & 0xFFFF;
			this.fixed = fixed & 0xFFFF;
			this.rssi = SikValueToDB(rssi & 0xFF);
			this.remrssi = SikValueToDB(remrssi & 0xFF);
			this.noise = SikValueToDB(noise & 0xFF);
			this.remnoise = SikValueToDB(remnoise & 0xFF);
			this.txbuf = txbuf & 0xFF;

			myDrone.events.notifyDroneEvent(DroneEventsType.RADIO);
		}

	}

	/**
	 * Scalling done at the Si1000 radio More info can be found at:
	 * http://copter
	 * .ardupilot.com/wiki/common-using-the-3dr-radio-for-telemetry-
	 * with-apm-and-px4/#Power_levels
	 */
	private double SikValueToDB(int value) {
		return (value / 1.9) - 127;
	}

}
