package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.List;

/**
 * Handle spline waypoint mavlink packet generation.
 */
public class SplineWaypointImpl extends SpatialCoordItem {

    /**
     * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
     * MISSION for rotary wing)
     */
    private double delay;

    public SplineWaypointImpl(MissionItemImpl item) {
        super(item);
    }

    public SplineWaypointImpl(MissionImpl missionImpl, LatLongAlt coord) {
        super(missionImpl, coord);
    }

    public SplineWaypointImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl, null);
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
