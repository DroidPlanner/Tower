package org.droidplanner.core.mission.waypoints;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

import junit.framework.TestCase;

public class WaypointTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		Waypoint item = new Waypoint(mission,
				new Coord3D(0, 1, new Altitude(2)));

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
