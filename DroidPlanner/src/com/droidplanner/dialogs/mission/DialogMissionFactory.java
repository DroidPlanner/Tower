package com.droidplanner.dialogs.mission;

import android.content.Context;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.drone.variables.waypoint;

public class DialogMissionFactory {

	public static void getDialog(waypoint wp, Context context,
			OnWaypointUpdateListner listner) {
		DialogMission dialog;
		switch (wp.getCmd()) {
		case CMD_NAV_WAYPOINT:
			dialog = new DialogMissionWaypoint();
			break;
		case CMD_NAV_LOITER_UNLIM:
			dialog = new DialogMissionLoiter();
			break;
		case CMD_NAV_LOITER_TURNS:
			dialog = new DialogMissionLoiterN();
			break;
		case CMD_NAV_LOITER_TIME:
			dialog = new DialogMissionLoiterT();
			break;
		case CMD_NAV_RETURN_TO_LAUNCH:
			dialog = new DialogMissionRTL();
			break;
		case CMD_NAV_TAKEOFF:
			dialog = new DialogMissionTakeoff();
			break;
		case CMD_NAV_LAND:
			dialog = new DialogMissionLand();
			break;
		default:
			dialog = new DialogMissionGeneric();
			break;
		}
		dialog.build(wp, context, listner);
	}

}
