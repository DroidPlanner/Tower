package org.droidplanner.android.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;

import android.view.View;

public class MissionRegionOfInterestFragment extends MissionDetailFragment
		implements SeekBarWithText.OnTextSeekBarChangedListener {

	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemType.ROI));

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(((RegionOfInterest) itemRender.getMissionItem()).getCoordinate()
                .getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		((RegionOfInterest) itemRender.getMissionItem()).setAltitude(new Altitude(altitudeSeekBar
				.getValue()));
	}

}
