package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.RadioButton;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetSpeed extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText speedSeekBar;
	private SeekBarWithText throttleSeekBar;
	private RadioButton airspeedRadioButton;
	private RadioButton groundspeedRadioButton;
	
	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_speed;
	}
	
	protected View buildView() {
		super.buildView();
		speedSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointSpeed);
		speedSeekBar.setValue(wp.getHeight());
		speedSeekBar.setOnChangedListner(this);

		throttleSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointThrottle);
		throttleSeekBar.setValue(wp.getHeight());
		throttleSeekBar.setOnChangedListner(this);
		
		airspeedRadioButton = (RadioButton) view
				.findViewById(R.id.radioAirSpeed);
		groundspeedRadioButton = (RadioButton) view
				.findViewById(R.id.radioGroundSpeed);
		if(wp.missionItem.param1==0)
				airspeedRadioButton.setChecked(true);
		else
				groundspeedRadioButton.setChecked(true);
		
		return view;
	}


	
	@Override
	public void onSeekBarChanged() {
		wp.missionItem.param2=(float) speedSeekBar.getValue();
		wp.missionItem.param3=(float) throttleSeekBar.getValue();
	}

}
