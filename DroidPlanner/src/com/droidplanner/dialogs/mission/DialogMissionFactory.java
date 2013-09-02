package com.droidplanner.dialogs.waypoints;

import android.content.Context;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.drone.variables.waypoint;

public class WaypointDialogFactory {

	public static void getDialog(waypoint wp, Context context,
			OnWaypointUpdateListner listner) {
		WaypointDialog dialog;
		switch (wp.getCmd()) {
		case CMD_NAV_WAYPOINT:
			dialog = new WaypointDialogWaypoint();
			break;
		case CMD_NAV_LOITER_UNLIM:
			dialog = new WaypointDialogLoiter();
			break;
		case CMD_NAV_LOITER_TURNS:
			dialog = new WaypointDialogLoiterN();
			break;
		case CMD_NAV_LOITER_TIME:
			dialog = new WaypointDialogLoiterT();
			break;
		case CMD_NAV_RETURN_TO_LAUNCH:
			dialog = new WaypointDialogRTL();
			break;
		case CMD_NAV_TAKEOFF:
			dialog = new WaypointDialogTakeoff();
			break;
		case CMD_NAV_LAND:
			dialog = new WaypointDialogLand();
			break;
		default:
			dialog = new WaypointDialogGeneric();
			break;
		}
		dialog.build(wp, context, listner);
	}

}
