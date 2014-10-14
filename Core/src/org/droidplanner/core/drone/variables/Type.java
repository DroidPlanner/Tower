package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.enums.MAV_TYPE;

public class Type extends DroneVariable {

	private int type = MAV_TYPE.MAV_TYPE_GENERIC;
	private String firmwareVersion = null;

	public Type(Drone myDrone) {
		super(myDrone);
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			myDrone.notifyDroneEvent(DroneEventsType.TYPE);
			myDrone.loadVehicleProfile();
		}
	}

	public int getType() {
		return type;
	}

	public FirmwareType getFirmwareType() {
		if (myDrone.getMavClient().isConnected()) {
			switch (this.type) {

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
		return myDrone.getPreferences().getVehicleType(); // offline or
															// unsupported
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String message) {
		firmwareVersion = message;
		myDrone.notifyDroneEvent(DroneEventsType.FIRMWARE);
	}

}