package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.core.mission.MissionItemType;

import android.os.Bundle;
import android.view.View;

public class MissionEpmGrabberFragment extends MissionDetailFragment {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_epm_grabber;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.EPM_GRIPPER));
	}
}
