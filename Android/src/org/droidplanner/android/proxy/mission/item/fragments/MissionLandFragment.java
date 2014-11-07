package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;

import android.os.Bundle;
import android.view.View;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

public class MissionLandFragment extends MissionDetailFragment {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_land;
	}

	@Override
	public void onApiConnected(DroneApi api){
        super.onApiConnected(api);
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LAND));
    }

}
