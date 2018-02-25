package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.commands.TakeoffImpl;

import java.util.List;

public class TakeoffImplTest extends TestCase {

    public void testPackMissionItem() {
        MissionImpl missionImpl = new MissionImpl(null);
        TakeoffImpl item = new TakeoffImpl(missionImpl, (50.0));

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_NAV_TAKEOFF, msg.command);
        assertEquals(50.0f, msg.z);
        assertEquals(0.0f, msg.param1);
        assertEquals(0.0f, msg.param2);
        assertEquals(0.0f, msg.param3);
        assertEquals(0.0f, msg.param3);
    }

}
