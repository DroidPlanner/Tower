package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

/**
 * Handle spline waypoint mavlink packet generation.
 */
public class SplineWaypoint extends SpatialCoordItem {

	/**
	 * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
	 * MISSION for rotary wing)
	 */
	private double delay;

	public SplineWaypoint(MissionItem item) {
		super(item);
	}

	public SplineWaypoint(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	public SplineWaypoint(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT;
		mavMsg.param1 = (float) delay;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setDelay(mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SPLINE_WAYPOINT;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}
}
