package org.droidplanner.fragments.mission;

import org.droidplanner.drone.variables.mission.waypoints.Takeoff;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;

import android.view.View;

import org.droidplanner.R;

public class MissionTakeoffFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListener {
	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_takeoff;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.TAKEOFF));

		Takeoff item = (Takeoff) this.item;

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);

	}

	@Override
	public void onSeekBarChanged() {
		Takeoff item = (Takeoff) this.item;
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
	}

}
