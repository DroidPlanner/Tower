package org.droidplanner.core.mission.waypoints;

import java.util.List;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.commands.Takeoff;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class TakeoffTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		Takeoff item = new Takeoff(mission, new Altitude(50.0));

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
