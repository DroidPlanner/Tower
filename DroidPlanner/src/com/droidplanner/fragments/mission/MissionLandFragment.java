package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Land;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionLandFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {

	//private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_detail_land;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		Land item = (Land) this.item;
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LAND));
		//yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		//yawSeekBar.setValue(item.getYawAngle());
		//yawSeekBar.setOnChangedListner(this);
	}

	@Override
	public void onSeekBarChanged() {
		Land item = (Land) this.item;
		//item.setYawAngle((float) yawSeekBar.getValue());
	}

}
