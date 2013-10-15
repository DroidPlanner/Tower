package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionWaypoint extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;
	private SeekBarWithText yawSeekBar;
	private SeekBarWithText radiusSeekBar;
	private SeekBarWithText orbitSeekBar;
	private CheckBox orbitCCW;

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

		delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
		delaySeekBar.setValue((double) wp.missionItem.param1);
		delaySeekBar.setOnChangedListner(this);

		radiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAcceptanceRadius);
		radiusSeekBar.setValue(wp.missionItem.param2);
		radiusSeekBar.setOnChangedListner(this);


		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(wp.missionItem.param4);
		yawSeekBar.setOnChangedListner(this);

		orbitSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointOrbitalRadius);
		orbitSeekBar.setOnChangedListner(this);
		orbitSeekBar.setAbsValue(wp.missionItem.param3);
		
		orbitCCW = (CheckBox) view
				.findViewById(R.id.waypoint_CCW);
		if (wp.missionItem.param3 < 0) {
			orbitCCW.setChecked(true);
		} else {
			orbitCCW.setChecked(false);
		}
		orbitCCW.setOnCheckedChangeListener(this);
		

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) delaySeekBar.getValue();
 		wp.missionItem.param2 = (float) radiusSeekBar.getValue();
		wp.missionItem.param4 = (float) yawSeekBar.getValue();
 
		wp.missionItem.param3 = (float) orbitSeekBar.getValue();
		
		if (orbitCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
		
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		wp.missionItem.param3 = (float) orbitSeekBar.getValue();
		if (orbitCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
	}
}
