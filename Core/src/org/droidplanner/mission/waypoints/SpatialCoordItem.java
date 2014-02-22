package org.droidplanner.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.coordinates.Coord3D;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.mission.Mission;
import org.droidplanner.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_FRAME;

public abstract class SpatialCoordItem extends MissionItem {

	protected Coord3D coordinate;
	
	public SpatialCoordItem(Mission mission, Coord3D coord) {
		super(mission);
		this.coordinate = coord;
	}

	public SpatialCoordItem(MissionItem item) {
		super(item);
		if (item instanceof SpatialCoordItem) {
			coordinate = ((SpatialCoordItem) item).getCoordinate();
		} else {
			coordinate = new Coord3D(0, 0, new Altitude(0));
		}
	}


	public void setCoordinate(Coord3D coordNew) {
		coordinate = coordNew;
	}

	public Coord3D getCoordinate() {
		return coordinate;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
			List<msg_mission_item> list = new ArrayList<msg_mission_item>();
			msg_mission_item mavMsg = new msg_mission_item();
			list.add(mavMsg);
			mavMsg.autocontinue = 1;
			mavMsg.target_component = 1;
			mavMsg.target_system = 1;
			mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
			mavMsg.x = (float) coordinate.getX();
			mavMsg.y = (float) coordinate.getY();
			mavMsg.z = (float) coordinate.getAltitude().valueInMeters();
	//		mavMsg.compid =
			return list;
		}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		Altitude alt = new Altitude(mavMsg.z);
		setCoordinate(new Coord3D(mavMsg.x,mavMsg.y, alt));
	}

}