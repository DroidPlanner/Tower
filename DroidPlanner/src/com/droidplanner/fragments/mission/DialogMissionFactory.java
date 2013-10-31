package com.droidplanner.dialogs.mission;

import android.content.Context;

import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.drone.variables.mission.MissionItem;

public class DialogMissionFactory {

	public static void getDialog(MissionItem item, Context context,
			OnWaypointChangedListner listner) {
		DialogMission dialog;
		dialog = item.getDialog();
		dialog.build(item, context, listner);
	}

}
