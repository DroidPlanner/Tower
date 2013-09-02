package com.droidplanner.dialogs.mission;

import android.content.Context;
import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionRTL extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.dialog_waypoint_rtl;
	}
	
	protected View buildView(Context context) {
		super.buildView(context);
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltitude);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
	}


}
