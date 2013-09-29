package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLoiterT extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTimeSeekBar;
	private SeekBarWithText loiterRadiusSeekBar;
	private CheckBox loiterCCW;
	private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_loitert;
	}
	
	protected View buildView() {
		super.buildView();

		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		if (wp.missionItem.param3 < 0) {
			loiterCCW.setChecked(true);
		} else {
			loiterCCW.setChecked(false);
		}
		loiterCCW.setOnCheckedChangeListener(this);

		
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);
		
		loiterTimeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListner(this);
		loiterTimeSeekBar.setValue(wp.missionItem.param1);
		
		loiterRadiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterRadius);
		loiterRadiusSeekBar.setAbsValue(wp.missionItem.param3);
		loiterRadiusSeekBar .setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(wp.missionItem.param4);
		yawSeekBar.setOnChangedListner(this);

		return view;
	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		wp.missionItem.param3 = (float) loiterRadiusSeekBar.getValue();
		if (loiterCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
    }
	

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) loiterTimeSeekBar.getValue();
		wp.missionItem.param3 = (float) loiterRadiusSeekBar.getValue();
		if (loiterCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
		wp.missionItem.param4 = (float) yawSeekBar.getValue();
	}


}
