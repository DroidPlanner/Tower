package org.droidplanner.mission.waypoints;

import java.util.List;

import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.mission.Mission;
import org.droidplanner.mission.MissionItem;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public abstract class LoiterInfinite extends Loiter {

	public LoiterInfinite(MissionItem item) {
		super(item);
	}

	public LoiterInfinite(Mission mission, LatLng coord, Altitude altitude) {
		super(mission, coord, altitude);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

}