package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;

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
		if (isValid()) {
			return GeoTools.getDistance(coordinate, myDrone.GPS.getPosition());
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
		myDrone.events.notifyDroneEvent(DroneEventsType.HOME);
	}

	public msg_mission_item packMavlink() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.current = 0;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		if (isValid()) {
			mavMsg.x = (float) getCoord().getLat();
			mavMsg.y = (float) getCoord().getLng();
			mavMsg.z = (float) getAltitude().valueInMeters();
		}
		
		return mavMsg;
	}

}
