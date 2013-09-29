package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionTakeoff extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText angleSeekBar;
	private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_takeoff;
	}
	
	protected View buildView() {
		super.buildView();
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);

		angleSeekBar = (SeekBarWithText) view
				.findViewById(R.id.takeoffPitch);
		angleSeekBar.setValue(wp.missionItem.param1);
		angleSeekBar.setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(wp.missionItem.param4);
		yawSeekBar.setOnChangedListner(this);

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) angleSeekBar.getValue();
		wp.missionItem.param4 = (float) yawSeekBar.getValue();
	}


}
