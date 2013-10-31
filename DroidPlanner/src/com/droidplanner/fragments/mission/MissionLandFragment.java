package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.Land;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionLandFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {

	private SeekBarWithText yawSeekBar;
	private Land item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_land;
	}

	@Override
	public void setItem(MissionItem item) {
		this.item =  (Land) item; 
	}
	
	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.yawAngle);
		yawSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		item.yawAngle = (float) yawSeekBar.getValue();
	}


}
