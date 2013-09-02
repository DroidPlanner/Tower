package com.droidplanner.dialogs.waypoints;

import android.content.Context;
import android.view.View;

import com.droidplanner.R;

public class WaypointDialogLoiter extends WaypointDialog {

	@Override
	protected int getResource() {
		return R.layout.dialog_waypoint_loiter;
	}
	
	protected View buildView(Context context) {
		super.buildView(context);		
		return view;
	}

}
