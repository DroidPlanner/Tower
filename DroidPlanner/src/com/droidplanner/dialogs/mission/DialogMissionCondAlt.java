package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionCondAlt extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText rateSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_cond_alt;
	}
	
	protected View buildView() {
		super.buildView();
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointFinalAlt);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);


		rateSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltRate);
		rateSeekBar .setValue((double)wp.missionItem.param1);
		rateSeekBar .setOnChangedListner(this);

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) rateSeekBar.getValue();
	}


}
