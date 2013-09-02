package com.droidplanner.dialogs.mission;

import android.content.Context;
import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLoiterT extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	SeekBarWithText loiterTimeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_waypoint_loitert;
	}
	
	protected View buildView(Context context) {
		super.buildView(context);
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltitude);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);
		
		loiterTimeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListner(this);
		loiterTimeSeekBar.setValue(wp.missionItem.param1);
		
		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) loiterTimeSeekBar.getValue();
	}


}
