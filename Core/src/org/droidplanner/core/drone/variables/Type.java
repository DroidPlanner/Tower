package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;

import com.MAVLink.Messages.enums.MAV_TYPE;

public class Type extends DroneVariable {

	private int type = MAV_TYPE.MAV_TYPE_GENERIC;

	public Type(Drone myDrone) {
		super(myDrone);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			myDrone.events.notifyDroneEvent(DroneEventsType.TYPE);
			myDrone.profile.load();
		}
	}

	public int getType() {
		return type;
	}

	public FirmwareType getFirmwareType() {
		if (myDrone.MavClient.isConnected()) {
			switch (myDrone.type.getType()) {
			case MAV_TYPE.MAV_TYPE_FIXED_WING:
				return FirmwareType.ARDU_PLANE;
			case MAV_TYPE.MAV_TYPE_GENERIC:
			case MAV_TYPE.MAV_TYPE_QUADROTOR:
			case MAV_TYPE.MAV_TYPE_COAXIAL:
			case MAV_TYPE.MAV_TYPE_HELICOPTER:
			case MAV_TYPE.MAV_TYPE_HEXAROTOR:
			case MAV_TYPE.MAV_TYPE_OCTOROTOR:
			case MAV_TYPE.MAV_TYPE_TRICOPTER:
				return FirmwareType.ARDU_COPTER;
			case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
			case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
				return FirmwareType.ARDU_ROVER;
			default:
				// unsupported - fall thru to offline condition
			}
		}
		return myDrone.preferences.getVehicleType(); // offline or unsupported
	}

	public enum FirmwareType {
		ARDU_PLANE("ArduPlane"), ARDU_COPTER("ArduCopter"), ARDU_ROVER(
				"ArduRover");

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
}