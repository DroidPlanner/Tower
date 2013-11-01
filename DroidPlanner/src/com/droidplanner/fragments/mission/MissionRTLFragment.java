package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.commands.ReturnToHome;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionRTLFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_detail_rtl;
	}
	

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.RTL));
		
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(((ReturnToHome) item).getHeight().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		((ReturnToHome) item).setHeight(new Altitude(altitudeSeekBar.getValue()));
	}

}
