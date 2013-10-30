package com.droidplanner.dialogs.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.commands.RegionOfInterest;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionRTL extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private RegionOfInterest item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_rtl;
	}
	
	protected View buildView() {
		super.buildView();
		item = (RegionOfInterest) wp;
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getHeight().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		item.setHeight(new Altitude(altitudeSeekBar.getValue()));
	}


}
