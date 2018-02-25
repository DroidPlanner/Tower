package co.aerobotics.android.proxy.mission.item.fragments;

import co.aerobotics.android.R;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;

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
