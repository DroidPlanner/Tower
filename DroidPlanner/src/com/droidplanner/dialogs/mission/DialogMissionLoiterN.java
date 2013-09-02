package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLoiterN extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText loiterTurnSeekBar;
	private CheckBox loiterCCW;

	@Override
	protected int getResource() {
		return R.layout.dialog_waypoint_loitern;
	}

	protected View buildView() {
		super.buildView();

		loiterTurnSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTurn);
		loiterTurnSeekBar.setOnChangedListner(this);

		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);

		if (wp.missionItem.param1 < 0) {
			loiterCCW.setChecked(true);
			loiterTurnSeekBar.setValue(-1.0 * wp.missionItem.param1);
		} else {
			loiterCCW.setChecked(false);
			loiterTurnSeekBar.setValue(wp.missionItem.param1);
		}

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.missionItem.param1 = (float) loiterTurnSeekBar.getValue();
		if (loiterCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
	}

}
