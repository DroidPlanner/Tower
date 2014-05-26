package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.Takeoff;

import android.os.Bundle;
import android.view.View;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener {
	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_takeoff;
	}

	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
