package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;

import java.util.List;

public class WaypointImplTest extends TestCase {

    public void testPackMissionItem() {
        MissionImpl missionImpl = new MissionImpl(null);
        WaypointImpl item = new WaypointImpl(missionImpl, new LatLongAlt(0, 1, (2)));

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_NAV_WAYPOINT, msg.command);
        assertEquals(0.0f, msg.param1);
        assertEquals(0.0f, msg.param2);
        assertEquals(0.0f, msg.param3);
        assertEquals(0.0f, msg.param3);
    }

}
