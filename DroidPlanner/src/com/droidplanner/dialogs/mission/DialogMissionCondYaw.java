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
		findIewItems();
		setupViewItems();
		setupViewListeners();

		
		return view;
	}

    private void findIewItems() {
		angleSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawRateSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointYawRate);
		yawDirCheckBox = (CheckBox) view.findViewById(R.id.checkBoxYawDir);
		yawOffsetCheckBox = (CheckBox) view.findViewById(R.id.checkBoxYawOffset);
	}

	private void setupViewItems() {
		angleSeekBar.setValue(wp.missionItem.param1);
		yawRateSeekBar.setValue(wp.missionItem.param2);
		yawDirCheckBox.setChecked(wp.missionItem.param3>0?true:false);
		yawOffsetCheckBox.setChecked(wp.missionItem.param4>0?true:false);
		
	}

	private void setupViewListeners() {
		angleSeekBar.setOnChangedListner(this);
		yawRateSeekBar.setOnChangedListner(this);
		yawDirCheckBox.setOnCheckedChangeListener(this);
		yawOffsetCheckBox.setOnCheckedChangeListener(this);
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
