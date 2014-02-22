package org.droidplanner.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.drone.variables.missionD.waypoints.LandD;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;

import android.view.View;

public class MissionLandFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListener {

	//private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_land;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		LandD item = (LandD) this.item;
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LAND));
		//yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		//yawSeekBar.setValue(item.getYawAngle());
		//yawSeekBar.setOnChangedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		LandD item = (LandD) this.item;
		//item.setYawAngle((float) yawSeekBar.getValue());
	}

}
