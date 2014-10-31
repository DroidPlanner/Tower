package org.droidplanner.core.firmware;

public enum FirmwareType {
	ARDU_PLANE("ArduPlane"), ARDU_COPTER("ArduCopter"), ARDU_ROVER("ArduRover");

	private final String type;

	FirmwareType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static FirmwareType firmwareFromString(String str) {
		if (str.equalsIgnoreCase(ARDU_PLANE.type)) {
			return ARDU_PLANE;
		}

		if (str.equalsIgnoreCase(ARDU_ROVER.type)) {
			return ARDU_ROVER;
		} else {
			return ARDU_COPTER;
		}
	}
}
