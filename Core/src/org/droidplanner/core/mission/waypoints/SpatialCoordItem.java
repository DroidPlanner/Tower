package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

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
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.x = (float) coordinate.getLat();
		mavMsg.y = (float) coordinate.getLng();
		mavMsg.z = (float) coordinate.getAltitude().valueInMeters();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		Altitude alt = new Altitude(mavMsg.z);
		setCoordinate(new Coord3D(mavMsg.x, mavMsg.y, alt));
	}

	public void setAltitude(Altitude altitude) {
		coordinate.set(coordinate.getLat(), coordinate.getLng(), altitude);
	}

	public void setPosition(Coord2D position) {
		coordinate.set(position);
	}

}