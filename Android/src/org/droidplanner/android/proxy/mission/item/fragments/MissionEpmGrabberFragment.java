package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;

import android.os.Bundle;
import android.view.View;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

public class MissionEpmGrabberFragment extends MissionDetailFragment {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_epm_grabber;
	}

	@Override
	public void onApiConnected() {
        super.onApiConnected();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.EPM_GRIPPER));
	}
}
