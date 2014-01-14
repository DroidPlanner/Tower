package org.droidplanner.fragments.mission;

import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

import android.view.View;

import org.droidplanner.R;

public class MissionRTLFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {
	//private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_rtl;
	}


	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.RTL));

		//altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		//altitudeSeekBar.setValue(((ReturnToHome) item).getHeight().valueInMeters());
		//altitudeSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		//((ReturnToHome) item).setHeight(new Altitude(altitudeSeekBar.getValue()));
	}

}
