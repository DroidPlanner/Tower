package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionWaypoint extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;
	private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_waypoint;
	}
	
	protected View buildView() {
		super.buildView();
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);


		delaySeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointDelay);
		delaySeekBar .setValue((double)wp.missionItem.param2);
		delaySeekBar .setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(wp.missionItem.param4);
		yawSeekBar.setOnChangedListner(this);

		
		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param2 = (float) delaySeekBar.getValue();
		wp.missionItem.param4 = (float) yawSeekBar.getValue();
	}


}
