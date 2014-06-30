package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.waypoints.Loiter;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MissionLoiterFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener, OnCheckedChangeListener{


	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_loiter;
	}


	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		//typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LOITER));

		//LoiterInfinite item = (LoiterInfinite) this.item;
	}



	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) itemRender.getMissionItem()).setOrbitCCW(isChecked);
    }

	@Override
	public void onSeekBarChanged() {
		//Takeoff item = (Takeoff) this.item;
		//((Loiter) item).setOrbitalRadius(loiterRadiusSeekBar.getValue());
	}
}
