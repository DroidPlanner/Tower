package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Radio extends DroneVariable {
	private short rxerrors = -1;
	private short fixed = -1;
	private byte rssi = -1;
	private byte remrssi = -1;
	private byte txbuf = -1;
	private byte noise = -1;
	private byte remnoise = -1;

	public Radio(Drone myDrone) {
		super(myDrone);
	}

	public short getRxErrors() {
		return rxerrors;
	}

	public short getFixed() {
		return fixed;
	}

	public byte getRssi() {
		return rssi;
	}

	public byte getRemRssi() {
		return remrssi;
	}

	public byte getTxBuf() {
		return txbuf;
	}

	public byte getNoise() {
		return noise;
	}

	public byte getRemNoise() {
		return remnoise;
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
