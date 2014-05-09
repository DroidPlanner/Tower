package org.droidplanner.android.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.Takeoff;

import android.view.View;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener {
	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_takeoff;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

		Takeoff item = (Takeoff) this.itemRender.getMissionItem();

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getFinishedAlt().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);

	}

	@Override
	public void onSeekBarChanged() {
		Takeoff item = (Takeoff) this.itemRender.getMissionItem();
		item.setFinishedAlt(new Altitude(altitudeSeekBar.getValue()));
	}

}
