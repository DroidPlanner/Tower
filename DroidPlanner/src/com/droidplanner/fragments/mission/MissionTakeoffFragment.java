package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.Takeoff;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionTakeoffFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText angleSeekBar;
	private SeekBarWithText yawSeekBar;
	private Takeoff item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_takeoff;
	}
	
	@Override
	public void setItem(MissionItem item) {
		this.item =  (Takeoff) item; 
	}	

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);

		angleSeekBar = (SeekBarWithText) view.findViewById(R.id.takeoffPitch);
		angleSeekBar.setValue(item.minPitch);
		angleSeekBar.setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.yawAngle);
		yawSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
		item.minPitch = angleSeekBar.getValue();
		item.yawAngle = yawSeekBar.getValue();
	}

}
