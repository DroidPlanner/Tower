package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.List;

public class WaypointImpl extends SpatialCoordItem {

	private double delay;
	private double acceptanceRadius;
	private double yawAngle;
	private double orbitalRadius;
	private boolean orbitCCW;

	public WaypointImpl(MissionItemImpl item) {
		super(item);
	}

	public WaypointImpl(MissionImpl missionImpl, LatLongAlt coord) {
		super(missionImpl, coord);
	}

	public WaypointImpl(msg_mission_item msg, MissionImpl missionImpl) {
		super(missionImpl, null);
		unpackMAVMessage(msg);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
		mavMsg.param1 = (float) getDelay();
		mavMsg.param2 = (float) getAcceptanceRadius();
		mavMsg.param3 = (float) (isOrbitCCW() ? getOrbitalRadius() * -1.0 : getOrbitalRadius());
		mavMsg.param4 = (float) getYawAngle();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setDelay(mavMsg.param1);
		setAcceptanceRadius(mavMsg.param2);
		setOrbitCCW(mavMsg.param3 < 0);
		setOrbitalRadius(Math.abs(mavMsg.param3));
		setYawAngle(mavMsg.param4);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.WAYPOINT;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public double getAcceptanceRadius() {
		return acceptanceRadius;
	}

	public void setAcceptanceRadius(double acceptanceRadius) {
		this.acceptanceRadius = acceptanceRadius;
	}

	public double getYawAngle() {
		return yawAngle;
	}

	public void setYawAngle(double yawAngle) {
		this.yawAngle = yawAngle;
	}

	public double getOrbitalRadius() {
		return orbitalRadius;
	}

	public void setOrbitalRadius(double orbitalRadius) {
		this.orbitalRadius = orbitalRadius;
	}

	public boolean isOrbitCCW() {
		return orbitCCW;
	}

	public void setOrbitCCW(boolean orbitCCW) {
		this.orbitCCW = orbitCCW;
	}

}