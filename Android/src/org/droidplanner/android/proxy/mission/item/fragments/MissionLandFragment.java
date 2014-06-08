package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Land;

import android.os.Bundle;
import android.view.View;

public class MissionLandFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener {

	//private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_land;
	}

	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		Land item = (Land) this.itemRender.getMissionItem();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LAND));
		//yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		//yawSeekBar.setValue(item.getYawAngle());
		//yawSeekBar.setOnChangedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		Land item = (Land) this.itemRender.getMissionItem();
		//item.setYawAngle((float) yawSeekBar.getValue());
	}

}
