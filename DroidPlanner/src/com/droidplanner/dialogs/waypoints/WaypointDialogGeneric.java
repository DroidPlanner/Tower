package com.droidplanner.dialogs.waypoints;

import android.content.Context;
import android.view.View;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class WaypointDialogGeneric extends WaypointDialog implements
		OnTextSeekBarChangedListner {
	SeekBarWithText altitudeSeekBar;

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

	@Override
	protected int getResource() {
		return R.layout.dialog_waypoint_generic;
	}

}
