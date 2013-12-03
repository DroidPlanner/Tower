package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.RegionOfInterest;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionRegionOfInterestFragment extends MissionDetailFragment
		implements OnTextSeekBarChangedListner {

	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemTypes.ROI));

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(((RegionOfInterest) item).getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		((RegionOfInterest) item).setAltitude(new Altitude(altitudeSeekBar
				.getValue()));
	}

}
