package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.model.Drone;

public class Radio extends DroneVariable implements DroneInterfaces.OnDroneListener {
	public static final int MAX_FADE_MARGIN = 50;
	public static final int MIN_FADE_MARGIN = 6;

	private double previousSignalStrength = 100;

	private boolean isValid = false;

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
		return (int) (MathUtil.Normalize(Math.min(getFadeMargin(), getRemFadeMargin()),
				MIN_FADE_MARGIN, MAX_FADE_MARGIN) * 100);
	}

	public void setRadioState(short rxerrors, short fixed, byte rssi, byte remrssi, byte txbuf,
			byte noise, byte remnoise) {
		isValid = true;
		if (this.rxerrors != rxerrors | this.fixed != fixed | this.rssi != rssi
				| this.remrssi != remrssi | this.txbuf != txbuf | this.noise != noise
				| this.remnoise != remnoise) {

			this.rxerrors = rxerrors & 0xFFFF;
			this.fixed = fixed & 0xFFFF;
			this.rssi = SikValueToDB(rssi & 0xFF);
			this.remrssi = SikValueToDB(remrssi & 0xFF);
			this.noise = SikValueToDB(noise & 0xFF);
			this.remnoise = SikValueToDB(remnoise & 0xFF);
			this.txbuf = txbuf & 0xFF;

			int currentSignalStrength = getSignalStrength();
			// if signal strength dips below 10%
			if (currentSignalStrength < 10.0 && previousSignalStrength >= 10.0) {
				myDrone.notifyDroneEvent(DroneEventsType.WARNING_SIGNAL_WEAK);
			}
			previousSignalStrength = currentSignalStrength;

			myDrone.notifyDroneEvent(DroneEventsType.RADIO);
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

	public boolean isValid() {
		return isValid;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case DISCONNECTED:
			isValid = false;
			break;
		}
	}
}
