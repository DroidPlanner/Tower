package org.droidplanner.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.drone.variables.mission.waypoints.RegionOfInterest;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;

import android.view.View;

public class MissionRegionOfInterestFragment extends MissionDetailFragment
		implements OnTextSeekBarChangedListener {

	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		//typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.ROI));

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(((RegionOfInterest) item).getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		((RegionOfInterest) item).setAltitude(new Altitude(altitudeSeekBar
				.getValue()));
	}

}
