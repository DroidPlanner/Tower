package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public class Home extends DroneVariable {
	private Coord2D coordinate;
	private Altitude altitude = new Altitude(0);

	public Home(Drone drone) {
		super(drone);
	}

	public boolean isValid() {
		return (coordinate != null);
	}

	public Home getHome() {
		return this;
	}

	public Length getDroneDistanceToHome() {
		if (isValid() && myDrone.getGps().isPositionValid()) {
			return GeoTools.getDistance(coordinate, myDrone.getGps().getPosition());
		} else {
			return new Length(0); // TODO fix this
		}
	}

	public Coord2D getCoord() {
		return coordinate;
	}

	public Length getAltitude() {
		return altitude;
	}

	public void setHome(msg_mission_item msg) {
		this.coordinate = new Coord2D(msg.x, msg.y);
		this.altitude = new Altitude(msg.z);
		myDrone.notifyDroneEvent(DroneEventsType.HOME);
	}

	public msg_mission_item packMavlink() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.current = 0;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		mavMsg.target_system = myDrone.getSysid();
		mavMsg.target_component = myDrone.getCompid();
		if (isValid()) {
			mavMsg.x = (float) getCoord().getLat();
			mavMsg.y = (float) getCoord().getLng();
			mavMsg.z = (float) getAltitude().valueInMeters();
		}

		return mavMsg;
	}

}
