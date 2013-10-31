package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Land;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLand extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText yawSeekBar;
	private Land item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_land;
	}

	protected View buildView() {
		super.buildView();
		item = (Land) wp;
		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.yawAngle);
		yawSeekBar.setOnChangedListner(this);
		return view;

	}

	@Override
	public void onSeekBarChanged() {
		item.yawAngle = (float) yawSeekBar.getValue();
	}
}
