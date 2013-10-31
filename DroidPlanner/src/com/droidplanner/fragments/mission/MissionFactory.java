package com.droidplanner.fragments.mission;

import android.content.Context;

import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.drone.variables.mission.MissionItem;

public class MissionFactory {

	public static void getDialog(MissionItem item, Context context,
			OnWaypointChangedListner listner) {
		MissionFragment dialog;
		dialog = item.getDialog();
		dialog.build(item, context, listner);
	}

}
