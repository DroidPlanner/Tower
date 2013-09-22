package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionCondYaw extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	private SeekBarWithText yawRateSeekBar;
	private SeekBarWithText angleSeekBar;
	private CheckBox yawDirCheckBox;
	private CheckBox yawOffsetCheckBox;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_cond_yaw;
	}
	
	protected View buildView() {
		super.buildView();
		angleSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		angleSeekBar.setValue(wp.missionItem.param1);
		angleSeekBar.setOnChangedListner(this);

		yawRateSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointYawRate);
		yawRateSeekBar.setValue(wp.missionItem.param2);
		yawRateSeekBar.setOnChangedListner(this);

		yawDirCheckBox = (CheckBox) view
				.findViewById(R.id.checkBoxYawDir);
		yawDirCheckBox.setChecked(wp.missionItem.param3>0?true:false);

		yawOffsetCheckBox = (CheckBox) view
				.findViewById(R.id.checkBoxYawOffset);
		yawOffsetCheckBox.setChecked(wp.missionItem.param4>0?true:false);
		
		return view;
	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		wp.missionItem.param3 = yawDirCheckBox.isChecked()?1:-1;
		wp.missionItem.param4 = yawOffsetCheckBox.isChecked()?1:0;
    }

    @Override
	public void onSeekBarChanged() {
		wp.missionItem.param1 = (float) angleSeekBar.getValue();
		wp.missionItem.param2 = (float) yawRateSeekBar.getValue();
	}


}
