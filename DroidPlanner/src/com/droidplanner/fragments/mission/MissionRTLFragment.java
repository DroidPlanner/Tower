package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.commands.ReturnToHome;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionRTLFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private ReturnToHome item;

	@Override
	protected int getResource() {
		return R.layout.fragment_detail_rtl;
	}
	
	@Override
	public void setItem(MissionItem item) {
		this.item =  (ReturnToHome) item; 
	}	

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getHeight().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		item.setHeight(new Altitude(altitudeSeekBar.getValue()));
	}

}
