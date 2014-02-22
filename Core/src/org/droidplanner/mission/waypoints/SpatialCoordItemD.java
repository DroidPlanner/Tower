package org.droidplanner.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.mission.Mission;
import org.droidplanner.mission.MissionItemD;
import org.droidplanner.mission.waypoints.SpatialCoordItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.google.android.gms.maps.model.LatLng;

public abstract class SpatialCoordItemD extends MissionItemD {

	protected LatLng coordinate;
	protected Altitude altitude;
	
	public SpatialCoordItemD(Mission mission, LatLng coord, Altitude altitude) {
		super(mission);
		this.coordinate = coord;
		this.altitude = altitude;
	}

	public SpatialCoordItemD(MissionItemD item) {
		super(item);
		if (item instanceof SpatialCoordItem) {
			coordinate = ((SpatialCoordItem) item).getCoordinate();
			altitude = ((SpatialCoordItem) item).getAltitude();
		} else {
			coordinate = new LatLng(0, 0);
			altitude = new Altitude(0);
		}
	}


	public void setCoordinate(LatLng position) {
		coordinate = position;
	}

	public LatLng getCoordinate() {
		return coordinate;
	}

	public Altitude getAltitude() {
		return altitude;
	}

	public void setAltitude(Altitude altitude) {
		this.altitude = altitude;
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
			mavMsg.x = (float) getCoordinate().latitude;
			mavMsg.y = (float) getCoordinate().longitude;
			mavMsg.z = (float) getAltitude().valueInMeters();
	//		mavMsg.compid =
			return list;
		}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		LatLng coord = new LatLng(mavMsg.x,mavMsg.y);
		Altitude alt = new Altitude(mavMsg.z);
		setCoordinate(coord);
		setAltitude(alt);
	}

}