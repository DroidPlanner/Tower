package org.droidplanner.core.mission.waypoints;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.Mission;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_FRAME;

public class SpatialCoordItemTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		Waypoint item = new Waypoint(mission, new Coord3D(0.1, 1, new Altitude(2)));

		msg_mission_item mavMsg = item.packMissionItem().get(0);

		assertEquals(1, mavMsg.autocontinue);
		assertEquals(MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT, mavMsg.frame);
		assertEquals(0.1f, mavMsg.x);
		assertEquals(1f, mavMsg.y);
		assertEquals(2f, mavMsg.z);
	}

}
