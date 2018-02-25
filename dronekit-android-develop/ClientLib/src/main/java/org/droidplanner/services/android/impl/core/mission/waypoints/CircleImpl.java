package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.ArrayList;
import java.util.List;

public class CircleImpl extends SpatialCoordItem {

    private double radius = 10.0;
    private int turns = 1;

    public CircleImpl(MissionItemImpl item) {
        super(item);
    }

    public CircleImpl(MissionImpl missionImpl, LatLongAlt coord) {
        super(missionImpl, coord);
    }

    public CircleImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl, null);
        unpackMAVMessage(msg);
    }

    public void setTurns(int turns) {
        this.turns = Math.abs(turns);
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getNumberOfTurns() {
        return turns;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = new ArrayList<msg_mission_item>();
        packSingleCircle(list);
        return list;
    }

    private void packSingleCircle(List<msg_mission_item> list) {
        msg_mission_item mavMsg = new msg_mission_item();
        list.add(mavMsg);
        mavMsg.autocontinue = 1;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.x = (float) coordinate.getLatitude();
        mavMsg.y = (float) coordinate.getLongitude();
        mavMsg.z = (float) (coordinate.getAltitude());
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
        mavMsg.param1 = Math.abs(turns);
        mavMsg.param3 = (float) radius;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        super.unpackMAVMessage(mavMsg);
        setTurns((int) mavMsg.param1);
        setRadius(mavMsg.param3);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.CIRCLE;
    }

}
