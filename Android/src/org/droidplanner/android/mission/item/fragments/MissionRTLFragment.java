package org.droidplanner.android.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.MissionItemType;

import android.view.View;

public class MissionRTLFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener {
	//private SeekBarWithText altitudeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_rtl;
	}


	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.RTL));

		//altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		//altitudeSeekBar.setValue(((ReturnToHome) item).getHeight().valueInMeters());
		//altitudeSeekBar.setOnChangedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		//((ReturnToHome) item).setHeight(new Altitude(altitudeSeekBar.getValue()));
	}

}
