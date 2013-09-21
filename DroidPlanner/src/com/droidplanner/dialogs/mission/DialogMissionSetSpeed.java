package com.droidplanner.dialogs.mission;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetSpeed extends DialogMission implements
		OnTextSeekBarChangedListner, OnClickListener {
	private SeekBarWithText speedSeekBar;
	private SeekBarWithText throttleSeekBar;
	private RadioButton airspeedRadioButton;
	private RadioButton groundspeedRadioButton;
	private RadioGroup radioGroupSpeed;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_speed;
	}

	protected View buildView() {
		super.buildView();
		findLocalViews();
		setupViews();
		setupListners();
		return view;
	}

	private void findLocalViews() {
		speedSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointSpeed);
		throttleSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointThrottle);
		radioGroupSpeed = (RadioGroup) view.findViewById(R.id.radioGroupSpeed);
		airspeedRadioButton = (RadioButton) view
				.findViewById(R.id.radioAirSpeed);
		groundspeedRadioButton = (RadioButton) view
				.findViewById(R.id.radioGroundSpeed);
	}

	private void setupViews() {
		speedSeekBar.setValue(wp.getHeight());
		throttleSeekBar.setValue(wp.getHeight());
		if (wp.missionItem.param1 == 0)
			airspeedRadioButton.setChecked(true);
		else
			groundspeedRadioButton.setChecked(true);
	}

	private void setupListners() {
		speedSeekBar.setOnChangedListner(this);
		throttleSeekBar.setOnChangedListner(this);
		radioGroupSpeed.setOnClickListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		wp.missionItem.param2 = (float) speedSeekBar.getValue();
		wp.missionItem.param3 = (float) throttleSeekBar.getValue();
	}

	@Override
	public void onClick(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		switch (view.getId()) {
		case R.id.radioAirSpeed:
			wp.missionItem.param1 = 0;
			if (checked)
				break;
		case R.id.radioGroundSpeed:
			if (checked)
				wp.missionItem.param1 = 1;
			break;
		}

	}

}
