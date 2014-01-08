package org.droidplanner.fragments.mission;

import org.droidplanner.drone.variables.mission.waypoints.Land;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

import android.view.View;

import org.droidplanner.R;

public class MissionLandFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner {

	//private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_land;
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
