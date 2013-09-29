package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionCondDistance extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText distanceSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_cond_distance;
	}
	
	protected View buildView() {
		super.buildView();
		distanceSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointDistance);
		distanceSeekBar.setValue(wp.missionItem.param1);
		distanceSeekBar.setOnChangedListner(this);

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.missionItem.param1 = (float) distanceSeekBar.getValue();
	}


}
